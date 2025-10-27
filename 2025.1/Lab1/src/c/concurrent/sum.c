#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

pthread_mutex_t lock;
long static total = 0;

void* do_sum(void* p) {
    char* path = (char*) p;
    FILE *file = fopen(path, "rb");  // open in binary mode
    if (file == NULL) {
        return NULL;  // indicate error
    }

    long sum = 0;
    int byte;

    while ((byte = fgetc(file)) != EOF) {
        sum += byte;
    }

    fclose(file);
    pthread_mutex_lock(&lock);
    total += sum;
    pthread_mutex_unlock(&lock);
    printf("%s : %d\n", path, sum);
}

int main(int argc, char *argv[]) {
    pthread_t threads[argc - 1];
    pthread_mutex_init(&lock, NULL);
    char* path;

    for (int i = 1; i < argc; i++)
    {
        path = argv[i];
        pthread_create(&threads[i-1], NULL, do_sum, (void*) path); 
    }

    for (int i = 0; i < argc - 1; i++)
    {
        pthread_join(threads[i], NULL);
    }
    
    printf("Total: %ld", total);
    
    return 0;
}
