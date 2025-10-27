import os
import sys
import threading

total = 0
sem = threading.Semaphore(1)

def do_sum(path):
    global total
    _sum = 0

    with open(path, 'rb') as f:
        byte = f.read(1)
        while byte:
            _sum += int.from_bytes(byte, byteorder='big', signed=False)
            byte = f.read(1)
        
        with sem:
            total += _sum
            print(path + " : " + str(_sum))

if __name__ == "__main__":
    paths = sys.argv[1:]
    threads = []
    for path in paths:
    #many error could be raised error. we don't care
        new_thread = threading.Thread(target=do_sum, args=(path,))
        threads.append(new_thread)
        new_thread.start()
    
    for thread in threads:
        thread.join()
    
    print(f"Total: {total} bytes")
        
