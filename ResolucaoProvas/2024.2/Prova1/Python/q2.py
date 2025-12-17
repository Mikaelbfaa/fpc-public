import threading
from threading import Semaphore

class SafeStack:
    def __init__(self, capacity = 1000) -> None:
        self.items: list = [None] * capacity
        self.pointer = -1
        self.mutex = Semaphore(1)
        self.items_available = Semaphore(0)
        self.empty_spaces = Semaphore(capacity)
    
    def isEmpty(self) -> bool:
        self.mutex.acquire()
        status = self.pointer == -1
        self.mutex.release()
        return status

    def push(self, value: int) -> None:
        self.empty_spaces.acquire()
        self.mutex.acquire()
        self.pointer += 1
        self.items[self.pointer] = value
        self.mutex.release()
        self.items_available.release()
    
    def pop(self) -> int:
        self.items_available.acquire()
        self.mutex.acquire()
        ret = self.items[self.pointer]
        self.pointer -= 1
        self.mutex.release()
        self.empty_spaces.release()
        
        return ret

def producer(stack, thread_id, num_items):
    for i in range(num_items):
        value = thread_id * 1000 + i
        stack.push(value)
        print(f"Thread {thread_id} pushed: {value}")

def consumer(stack, thread_id, num_items):
    for i in range(num_items):
        value = stack.pop()
        print(f"Thread {thread_id} popped: {value}")


if __name__ == "__main__":
    stack = SafeStack(capacity=100)
    
    threads = []
    
    for i in range(3):
        t = threading.Thread(target=producer, args=(stack, i, 10))
        threads.append(t)
        t.start()
    
    for i in range(3):
        t = threading.Thread(target=consumer, args=(stack, i, 10))
        threads.append(t)
        t.start()

    for t in threads:
        t.join()
    
    print(f"\nPilha vazia? {stack.isEmpty()}")
    print("Teste concluído com sucesso!")
