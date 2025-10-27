#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>

void* foo(void* valor) {
    int* v = (int*) valor;

    printf("Thread is printing value: %d\n", *v);

    return NULL;
}

void* foo2(void* valor) {
    int* v = (int*) valor;
    printf("Thread is returning value: %d\n", *v);

    int* resultado = malloc(sizeof(int));
    *resultado = (*v) * 2;

    pthread_exit((void*) resultado);
}

int main(int argc, char const *argv[])
{
    pthread_t thread_id;
    int valor = 42;
    int* retorno;

    if(pthread_create(&thread_id, NULL, foo, (void*) &valor) != 0) {
        printf("Thread 1 had a unexpected error while opening");
        return 1;
    }

    pthread_join(thread_id, NULL);

    if(pthread_create(&thread_id, NULL, foo2, (void*) &valor) != 0) {
        printf("Thread 2 had a unexpected error while opening");
        return 1;
    }

    pthread_join(thread_id, (void**) &retorno);

    printf("Double value: %d\n", *retorno);

    return 0;
}
