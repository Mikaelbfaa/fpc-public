from threading import Semaphore, Thread

NUM_BUCKETS = 1024

class Node:
    def __init__(self, key, value) -> None:
        self.key = key
        self.value = value
        self.next: Node | None = None
        

class SafeLinkedList:
    def __init__(self) -> None:
        self.lock = Semaphore()
        self.head: Node | None = None
    
    def add(self, key, value):
        self.lock.acquire()

        current = self.head
        
        while current is not None and current.next is not None:
            if current.key == key:
                current.value = value
                self.lock.release()
                return
            current = current.next
        
        if current is None:
            self.head = Node(key, value)
        elif current.key == key:
            current.value = value
        else:
            current.next = Node(key, value)
        
        self.lock.release()
    
    def contain(self, value):
        self.lock.acquire()

        current = self.head

        while current is not None:
            if current.value == value:
                self.lock.release()
                return True
            current = current.next
        
        self.lock.release()
        return False

class HashMap:
    def __init__(self) -> None:
        self.buckets = [SafeLinkedList() for _ in range(1024)]
        self.lock = Semaphore(1)
    
    def _hash(self, key):
        return key % NUM_BUCKETS
    
    def put(self, key, value):
        key_hash = self._hash(key)
        self.buckets[key_hash].add(key, value)
    
    def containsKey(self, key):
        key_hash = self._hash(key)
        bucket = self.buckets[key_hash]
        
        bucket.lock.acquire()
        current = bucket.head
        found = False
        while current is not None:
            if current.key == key:
                found = True
                break
            current = current.next
        bucket.lock.release()
        
        return found