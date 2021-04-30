#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <semaphore.h>
#include <sys/mman.h>
#include <SDL2/SDL.h>
#include <SDL2/SDL_mixer.h>


Mix_Music *gMusic = NULL;


void playMusic(const char * filename)
{
/*
	@brief:
	Play music file requested by passing the _File_Name_

	@description:
	This function checks if the specified file exists or
	not, and throws an error if file can't be found.  If
	the file exists, then it plays the music file, if no
	file is already being played.  It throws an error if
	a file is already being played.
*/
//printf("[ DEBUG ] : %s\n", filename);
if( Mix_OpenAudio( 44100, MIX_DEFAULT_FORMAT, 2, 2048 ) < 0 ) {
	printf("Audio couldn't be initialized properly\n");
}
gMusic = Mix_LoadMUS(filename);
if( gMusic == NULL )
	printf("Couldn't load the specified file\n");
else  if (Mix_PlayingMusic()==1)
	printf("Already playing some music\n");
else  if (Mix_PlayingMusic()==0)
	Mix_PlayMusic(gMusic,1);
}



void child(char *path, int id, void *mem)
{
	char name[11];
	snprintf(name, 11, "/shmfile%02d", id);
	sem_t *parent_sem = sem_open("/shmfile00", 0);
	sem_t *child_sem = sem_open(name, 0);
	playMusic(path);

	int volume = 128;

	while (1) {
		char operation;
		sem_wait(child_sem);
		memcpy(&operation, mem, sizeof(char));
		if (operation == 'm') {
			if (Mix_VolumeMusic(-1) == 0) {
				Mix_VolumeMusic(volume);
			}
			else {
				volume = Mix_VolumeMusic(0);
			}
		}
		else if (operation == 'v') {
			memcpy(&volume, mem + sizeof(char), sizeof(int));
			Mix_VolumeMusic((int)((float)volume / 100.0 * 128.0));
		}
		else if (operation == 'p') {
			if (Mix_PausedMusic() == 0) {
				Mix_PauseMusic();
			}
			else {
				Mix_ResumeMusic();
			}
		}
		sem_post(parent_sem);
	}
}



void parent(sem_t **sems, void *mem)
{
	while (1) {
		char operation;
		int track_number;
		sem_wait(sems[0]);
		printf(">>> ");
		scanf("%c%d", &operation, &track_number);
		while(getchar() != '\n');
		memcpy(mem, &operation, sizeof(char));
		if (operation == 'v') {
			int volume;
			scanf("%d", &volume);
			while(getchar() != '\n');
			memcpy(mem + sizeof(char), &volume, sizeof(int));
		}
		sem_post(sems[track_number]);
	}
}



int main(int argc, char **argv)
{
	int protection = PROT_READ | PROT_WRITE;
	int visibility = MAP_ANONYMOUS | MAP_SHARED;
	void *mem = mmap(NULL, 32, protection, visibility, 0, 0);

	sem_t *sems[argc];
	sem_unlink("/shmfile00");
	char *name = strdup("/shmfile00");
	sems[0] = sem_open(name, O_CREAT, 0666, 1);

	for (int i = 1; i < argc; ++i) {
		sprintf(name, "/shmfile%02d", i);
		sem_unlink(name);
		sems[i] = sem_open(name, O_CREAT, 0666, 0);
	}

	for (int i = 1; i < argc; ++i) {
		pid_t pid = fork();
		if (pid == 0) {
			child(argv[i], i, mem);
			break;
		}
	}

	parent(sems, mem);

	return 0;
}


