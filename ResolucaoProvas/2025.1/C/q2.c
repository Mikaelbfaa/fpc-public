#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <semaphore.h>
#include <unistd.h>
#include <time.h>

#define M 100
#define CONSUMER_N 8

int buffer[M];
int buffer_size = M;
int buffer_index = M - 1;
sem_t mutex;
sem_t producer;
sem_t consumer;

int getJob(pthread_t tid) {
    printf("Consumer %lu getting a job\n", (unsigned long)tid);
    int job = buffer[buffer_index];
    buffer_index--;
    return job;
}

void execute(int j) {
    printf("Executing task: %d\n", j);
    sleep(1);
}

void* compute(void* arg) {
    while(1) {
        sem_wait(&mutex);

        if(buffer_size == 0) {
            sem_post(&producer);
            sem_post(&mutex);
            sem_wait(&consumer);
            sem_wait(&mutex);
        }

        int j = getJob(pthread_self());
        buffer_size -= 1;
        sem_post(&mutex);

        execute(j);
    }
    return NULL;
}

void create_jobs(int M) {
    printf("Starting job creation\n");
    for(int i = 0; i < M; i++) {
        buffer[i] = i + 1;
    }
    buffer_index = M - 1;
    sleep(2);
    printf("Job creating done!\n");
}

void* manage(void* arg) {
    while(1) {
        sem_wait(&producer);
        create_jobs(M);
        buffer_size = M;
        sem_post(&consumer);
    }
    return NULL;
}

int main() {
    pthread_t consumerThreads[CONSUMER_N];
    pthread_t producerThread;

    sem_init(&mutex, 0, 1);
    sem_init(&producer, 0, 0);
    sem_init(&consumer, 0, 0);

    for(int i = 0; i < M; i++) {
        buffer[i] = i + 1;
    }

    pthread_create(&producerThread, NULL, manage, NULL);

    for(int i = 0; i < CONSUMER_N; i++) {
        pthread_create(&consumerThreads[i], NULL, compute, NULL);
    }

    pthread_join(producerThread, NULL);
    for(int i = 0; i < CONSUMER_N; i++) {
        pthread_join(consumerThreads[i], NULL);
    }

    sem_destroy(&mutex);
    sem_destroy(&producer);
    sem_destroy(&consumer);

    return 0;
}
