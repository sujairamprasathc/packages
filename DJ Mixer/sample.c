#include <stdio.h>
#include <unistd.h>
#include <semaphore.h>


int main()
{
	int pid = fork();

	sem_t parent_sem;
	sem_init(&parent_sem, 1, 0);
	sem_t child_sem;
	sem_init(&child_sem, 1, 1);

    if (pid) {
       int counter = 0;
       while (1) {
           sem_wait(&child_sem);
           counter += 1;
           printf("P%d = %d\n", pid, counter);
           sem_post(&parent_sem);
       }
     } else {
       int counter = 0;
       while (1) {
            sem_wait(&parent_sem);
            counter += 1;
            printf("P%d = %d\n", pid, counter);
            sem_post(&child_sem);
       }
     }
}
