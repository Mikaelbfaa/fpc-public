from threading import Semaphore, Lock, Thread, current_thread
import time

MAX_SIZE = 100
messageList = []
empty_spaces = Semaphore(MAX_SIZE)
filled_spaces = Semaphore(0)
mutex = Lock()

def putMessage(message):
    print(f"[Producer {current_thread().name}] Tentando enviar: {message}")
    empty_spaces.acquire()
    with mutex:
        messageList.append(message)
        print(f"[Producer {current_thread().name}] Mensagem enviada: {message}")
    filled_spaces.release()

def takeMessage():
    print(f"[Consumer {current_thread().name}] Tentando receber mensagem")
    filled_spaces.acquire()
    with mutex:
        msg = messageList.pop(0)
        print(f"[Consumer {current_thread().name}] Mensagem recebida: {msg}")
    empty_spaces.release()
    return msg

def producer(num_messages):
    for i in range(num_messages):
        message = f"Msg-{current_thread().name}-{i}"
        putMessage(message)
        time.sleep(0.1)

def consumer(num_messages):
    for i in range(num_messages):
        msg = takeMessage()
        time.sleep(0.15)

if __name__ == "__main__":
    NUM_PRODUCERS = 3
    NUM_CONSUMERS = 2
    MESSAGES_PER_PRODUCER = 5
    
    producers = []
    consumers = []
    
    print("Iniciando produtores...")
    for i in range(NUM_PRODUCERS):
        t = Thread(target=producer, args=(MESSAGES_PER_PRODUCER,), name=f"P{i}")
        producers.append(t)
        t.start()
    
    print("Iniciando consumidores...")
    for i in range(NUM_CONSUMERS):
        t = Thread(target=consumer, args=(NUM_PRODUCERS * MESSAGES_PER_PRODUCER // NUM_CONSUMERS,), name=f"C{i}")
        consumers.append(t)
        t.start()
    
    for t in producers:
        t.join()
    
    for t in consumers:
        t.join()
    
    print("Todas as threads finalizaram")
    print(f"Mensagens restantes no canal: {len(messageList)}")