#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <semaphore.h>

typedef struct {
    int* items;
    int pointer;
    sem_t mutex;
    sem_t items_available;
    sem_t empty_spaces;
    int capacity;
} SafeStack;

void safestack_init(SafeStack* stack, int capacity) {
    stack->items = (int*)malloc(capacity * sizeof(int));
    stack->pointer = -1;
    stack->capacity = capacity;
    sem_init(&stack->mutex, 0, 1);
    sem_init(&stack->items_available, 0, 0);
    sem_init(&stack->empty_spaces, 0, capacity);
}

int isEmpty(SafeStack* stack) {
    sem_wait(&stack->mutex);
    int status = stack->pointer == -1;
    sem_post(&stack->mutex);
    return status;
}

void push(SafeStack* stack, int value) {
    sem_wait(&stack->empty_spaces);
    sem_wait(&stack->mutex);
    stack->pointer += 1;
    stack->items[stack->pointer] = value;
    sem_post(&stack->mutex);
    sem_post(&stack->items_available);
}

int pop(SafeStack* stack) {
    sem_wait(&stack->items_available);
    sem_wait(&stack->mutex);
    int ret = stack->items[stack->pointer];
    stack->pointer -= 1;
    sem_post(&stack->mutex);
    sem_post(&stack->empty_spaces);

    return ret;
}

typedef struct {
    SafeStack* stack;
    int thread_id;
    int num_items;
} ThreadArgs;

void* producer(void* arg) {
    ThreadArgs* args = (ThreadArgs*)arg;
    for(int i = 0; i < args->num_items; i++) {
        int value = args->thread_id * 1000 + i;
        push(args->stack, value);
        printf("Thread %d pushed: %d\n", args->thread_id, value);
    }
    free(args);
    return NULL;
}

void* consumer(void* arg) {
    ThreadArgs* args = (ThreadArgs*)arg;
    for(int i = 0; i < args->num_items; i++) {
        int value = pop(args->stack);
        printf("Thread %d popped: %d\n", args->thread_id, value);
    }
    free(args);
    return NULL;
}

int main() {
    SafeStack stack;
    safestack_init(&stack, 100);

    pthread_t threads[6];

    for(int i = 0; i < 3; i++) {
        ThreadArgs* args = malloc(sizeof(ThreadArgs));
        args->stack = &stack;
        args->thread_id = i;
        args->num_items = 10;
        pthread_create(&threads[i], NULL, producer, args);
    }

    for(int i = 0; i < 3; i++) {
        ThreadArgs* args = malloc(sizeof(ThreadArgs));
        args->stack = &stack;
        args->thread_id = i;
        args->num_items = 10;
        pthread_create(&threads[3 + i], NULL, consumer, args);
    }

    for(int i = 0; i < 6; i++) {
        pthread_join(threads[i], NULL);
    }

    printf("\nPilha vazia? %s\n", isEmpty(&stack) ? "True" : "False");
    printf("Teste concluído com sucesso!\n");

    free(stack.items);
    sem_destroy(&stack.mutex);
    sem_destroy(&stack.items_available);
    sem_destroy(&stack.empty_spaces);

    return 0;
}
