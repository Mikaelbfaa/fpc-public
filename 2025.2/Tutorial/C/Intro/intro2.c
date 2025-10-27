#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>

#define NUM_THREADS 4

int sum_total = 0;
pthread_mutex_t lock;

typedef struct {
    int value;
    int thread_id;
} thread_args_t;


void* increment(void* arg) {
    thread_args_t* thread_args = (thread_args_t*) arg;
    int value = thread_args->value;
    int thread_id = thread_args->thread_id;
    printf("Thread %d sleeping for %d seconds\n", thread_id, value);
    sleep(value);
    printf("Thread %d woke up!\n", thread_id);

    pthread_mutex_lock(&lock);
    sum_total += value;
    printf("Thread %d added a total of %d\n", thread_id, value);
    pthread_mutex_unlock(&lock);
    printf("Thread %d is freeing memory\n", thread_id);
    free(thread_args);

    return NULL;
}

int main(int argc, char const *argv[])
{
    pthread_t thread_ids[NUM_THREADS];
    pthread_mutex_init(&lock, NULL);

    for (int i = 0; i < NUM_THREADS; i++) {
        thread_args_t* args = malloc(sizeof(thread_args_t));
        args->value = (rand() % 10) + 1;
        args->thread_id = i;

        pthread_create(&thread_ids[i], NULL, increment, (void*) args);
    }

    for (int i = 0; i < NUM_THREADS; i++) {
        pthread_join(thread_ids[i], NULL);
    }

    printf("The total sum was: %d\n", sum_total);

    return 0;
}
