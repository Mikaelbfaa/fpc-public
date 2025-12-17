#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <semaphore.h>
#include <unistd.h>
#include <time.h>

#define THREAD_NUMBER 8

int join_counter = 0;
sem_t mutex;
sem_t join_sem;
int sleep_times[THREAD_NUMBER];

void* foo(void* arg) {
    int thread_id = *(int*)arg;

    int sleep_time = (rand() % 5) + 1;
    sleep(sleep_time);
    printf("Thread %d slept for %d seconds\n", thread_id, sleep_time);

    sem_wait(&mutex);

    sleep_times[join_counter] = sleep_time;
    join_counter += 1;
    if(join_counter == THREAD_NUMBER) {
        sem_post(&join_sem);
    }

    sem_post(&mutex);

    free(arg);
    return NULL;
}

int main() {
    srand(time(NULL));
    pthread_t threads[THREAD_NUMBER];

    sem_init(&mutex, 0, 1);
    sem_init(&join_sem, 0, 0);

    for(int i = 0; i < THREAD_NUMBER; i++) {
        int* id = malloc(sizeof(int));
        *id = i + 1;
        pthread_create(&threads[i], NULL, foo, id);
    }

    for(int i = 0; i < THREAD_NUMBER; i++) {
        pthread_join(threads[i], NULL);
    }

    sem_wait(&join_sem);

    printf("Total wait times: [");
    for(int i = 0; i < THREAD_NUMBER; i++) {
        printf("%d", sleep_times[i]);
        if(i < THREAD_NUMBER - 1) printf(", ");
    }
    printf("]\n");

    int max_n = sleep_times[0];
    int min_n = sleep_times[0];
    for(int i = 1; i < THREAD_NUMBER; i++) {
        if(sleep_times[i] > max_n) max_n = sleep_times[i];
        if(sleep_times[i] < min_n) min_n = sleep_times[i];
    }

    printf("Min + Max = %d\n", max_n + min_n);

    sem_destroy(&mutex);
    sem_destroy(&join_sem);

    return 0;
}
