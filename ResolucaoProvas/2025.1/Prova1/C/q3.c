#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <semaphore.h>
#include <unistd.h>
#include <time.h>

typedef struct {
    int counter;
    sem_t mutex;
} LightSwitch;

sem_t turnstile;
sem_t isEmpty;
LightSwitch ls;

void lightswitch_init(LightSwitch* ls) {
    ls->counter = 0;
    sem_init(&ls->mutex, 0, 1);
}

void lightswitch_lock(LightSwitch* ls, sem_t* sem) {
    sem_wait(&ls->mutex);
    ls->counter += 1;
    if(ls->counter == 1) {
        sem_wait(sem);
    }
    sem_post(&ls->mutex);
}

void lightswitch_unlock(LightSwitch* ls, sem_t* sem) {
    sem_wait(&ls->mutex);
    ls->counter -= 1;
    if(ls->counter == 0) {
        sem_post(sem);
    }
    sem_post(&ls->mutex);
}

void lookup(pthread_t tid) {
    printf("Reader %lu is looking up on db\n", (unsigned long)tid);
}

void* safe_lookup(void* arg) {
    sem_wait(&turnstile);
    sem_post(&turnstile);

    lightswitch_lock(&ls, &isEmpty);
    lookup(pthread_self());
    lightswitch_unlock(&ls, &isEmpty);

    return NULL;
}

void update_db() {
    printf("updating\n");
    sleep(2);
    printf("done updating\n");
}

void* safe_update(void* arg) {
    sem_wait(&turnstile);
    sem_wait(&isEmpty);
    printf("Updating, no one should read or write\n");
    update_db();
    printf("Done\n");
    sem_post(&isEmpty);
    sem_post(&turnstile);

    return NULL;
}

int main() {
    srand(time(NULL));
    pthread_t readerThreads[10];
    pthread_t writerThreads[2];
    pthread_t allThreads[12];

    sem_init(&turnstile, 0, 1);
    sem_init(&isEmpty, 0, 1);
    lightswitch_init(&ls);

    for(int i = 0; i < 10; i++) {
        allThreads[i] = readerThreads[i];
    }
    for(int i = 0; i < 2; i++) {
        allThreads[10 + i] = writerThreads[i];
    }

    for(int i = 11; i > 0; i--) {
        int j = rand() % (i + 1);
        pthread_t temp = allThreads[i];
        allThreads[i] = allThreads[j];
        allThreads[j] = temp;
    }

    for(int i = 0; i < 10; i++) {
        pthread_create(&allThreads[i], NULL, safe_lookup, NULL);
    }
    for(int i = 10; i < 12; i++) {
        pthread_create(&allThreads[i], NULL, safe_update, NULL);
    }

    for(int i = 0; i < 12; i++) {
        pthread_join(allThreads[i], NULL);
    }

    sem_destroy(&turnstile);
    sem_destroy(&isEmpty);
    sem_destroy(&ls.mutex);

    return 0;
}
