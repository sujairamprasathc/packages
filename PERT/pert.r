data = read.csv('~/Workspace/Package/PERT/input_data.csv', header=F)

n = data[1,1]

a = matrix(rep(-1, times=n*n), nrow = n)
for (i in 2:nrow(data)) {
  a[data[i,1], data[i,2]] = data[i,3]
}

box = rep(0, n)

for (i in 2:n) {
  box[i] = max(box[a[,i] >= 0] + a[a[,i] >= 0,i])
}

delta = rep(0, n)
delta[n] = box[n]
for (i in seq(n-1,1,-1)) {
  delta[i] = min(delta[a[i,] >= 0] - a[i, a[i,] >= 0])
}

duration = 0

total_float = matrix(rep(0, n*n), nrow = 7)
free_float = matrix(rep(0, n*n), nrow = 7)

for (i in 1:n) {
  for (j in 1:n) {
    if (delta[i] == box[i] && delta[j] == box[j] && delta[j] - box[i] == a[i,j]) {
      cat('(', i, ',', j, ')', 'is a critical activity and must start at', box[i], '\n')
      duration = duration + a[i,j]
    } else if (a[i,j] >= 0) {
      total_float[i, j] = delta[j] - box[i] - a[i, j]
      free_float[i, j] = box[j] - box[i] - a[i, j]
      cat('(', i, ',', j, ')', 'is a non critical activity and can be scheduled between ')

      if (free_float[i, j] < total_float[i, j]) {
        cat('(', box[i], ',', box[i] + free_float[i, j], ')', '\n')
      } else {
        cat('(', box[i], ',', delta[j] - a[i,j], ')', '\n')
      }
    }
  }
}

print('Duration of project')
print(duration)





