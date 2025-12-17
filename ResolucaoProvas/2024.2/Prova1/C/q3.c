#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <semaphore.h>
#include <unistd.h>
#include <time.h>

#define SIZE 1000
#define N_PRODUCERS 4
#define N_CONSUMERS 2

typedef struct {
    int* requests;
    int count;
    sem_t producer;
    sem_t consumer;
} Broker;

Broker broker;

void broker_init() {
    broker.requests = (int*)malloc(SIZE * sizeof(int));
    broker.count = 0;
    sem_init(&broker.producer, 0, SIZE);
    sem_init(&broker.consumer, 0, 0);
}

void submitRequest(int r) {
    sem_wait(&broker.producer);
    broker.requests[broker.count] = r;
    broker.count++;
    printf("Master is appending %d to requests\n", r);
    sem_post(&broker.consumer);
}

void getWork() {
    sem_wait(&broker.consumer);
    broker.count--;
    int request = broker.requests[broker.count];
    printf("Worker is processing %d\n", request);
    sem_post(&broker.producer);
}

void* workerHandler(void* arg) {
    while(1) {
        getWork();
        sleep(1);
    }
    return NULL;
}

void* masterHandler(void* arg) {
    while(1) {
        submitRequest(rand());
        sleep(1);
    }
    return NULL;
}

int main() {
    srand(time(NULL));
    pthread_t threads[N_PRODUCERS + N_CONSUMERS];
    pthread_t temp_threads[N_PRODUCERS + N_CONSUMERS];

    broker_init();

    for(int i = 0; i < N_PRODUCERS; i++) {
        pthread_create(&temp_threads[i], NULL, masterHandler, NULL);
    }

    for(int i = 0; i < N_CONSUMERS; i++) {
        pthread_create(&temp_threads[N_PRODUCERS + i], NULL, workerHandler, NULL);
    }

    for(int i = 0; i < N_PRODUCERS + N_CONSUMERS; i++) {
        threads[i] = temp_threads[i];
    }

    for(int i = N_PRODUCERS + N_CONSUMERS - 1; i > 0; i--) {
        int j = rand() % (i + 1);
        pthread_t temp = threads[i];
        threads[i] = threads[j];
        threads[j] = temp;
    }

    for(int i = 0; i < N_PRODUCERS + N_CONSUMERS; i++) {
        pthread_join(threads[i], NULL);
    }

    free(broker.requests);
    sem_destroy(&broker.producer);
    sem_destroy(&broker.consumer);

    return 0;
}
