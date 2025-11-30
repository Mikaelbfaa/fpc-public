import random
import threading

threads = [0,1]

def getId():
    return random.randint(0,1)

class LockOne:
    def __init__(self) -> None:
        self.flags = [False, False]
    
    def lock(self):
        id = getId()
        self.flags[id] = True
        while self.flags[1 - id]:
            x = 0
    
    def unlock(self):
        id = getId()
        self.flags[id] = False

class LockTwo:
    def __init__(self) -> None:
        self.victim = -1
    
    def lock(self):
        id = random.randint(0,1)
        victim = id
        while victim == id:
            x = 0
    
    def unlock(self):
        id = getId()
        self.victim = id

class PetersonLock:
    def __init__(self) -> None:
        self.flags = [False, False]
        self.victim = -1
    
    def lock(self):
        id = getId()
        self.victim = id
        self.flags[id] = True
        while (self.victim == id) and self.flags[1 - id] == True:
            x = 0
    
    def unlock(self):
        id = getId()
        self.flags[id] = False
        self.victim = id