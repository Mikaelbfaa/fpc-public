class Bakery:
    def __init__(self, n):
        self.n = n
        self.tickets = [0 for _ in range(n)]
        self.choosing = [False for _ in range(n)]

    def lock(self, id):
        self.choosing[id] = True
        self.tickets[id] = max(self.tickets) + 1
        self.choosing[id] = False

        for i in range(self.n):
            while(self.choosing[i]):
                pass
            
            while(self.tickets[i] != 0 and (self.tickets[id] > self.tickets[i] or (self.tickets[id] == self.tickets[i] and id > i))):
                pass
    
    def unlock(self, id):
        self.tickets[id] = 0
            
