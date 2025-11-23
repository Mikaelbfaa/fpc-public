import threading
import time
from threading import Semaphore
import random

class Node:
    def __init__(self, value):
        self.value = value
        self.left = None
        self.right = None
        self.sem = Semaphore(1)

class BinarySearchTree:
    def __init__(self):
        self.root = None
        self.root_lock = Semaphore(1)
    
    def insert(self, n):
        self.root_lock.acquire()
        if self.root == None:
            self.root = Node(n)
            self.root_lock.release()
            return
        
        current = self.root
        current.sem.acquire()
        self.root_lock.release()
        
        while True:
            if current.value > n:
                if current.left == None:
                    current.left = Node(n)
                    current.sem.release()
                    return
                else:
                    current.left.sem.acquire()
                    current.sem.release()
                    current = current.left
            elif current.value < n:
                if current.right == None:
                    current.right = Node(n)
                    current.sem.release()
                    return
                else:
                    current.right.sem.acquire()
                    current.sem.release()
                    current = current.right
            else:
                current.sem.release()
                return
    
    def search(self, n):
        self.root_lock.acquire()
        if self.root == None:
            self.root_lock.release()
            return False
        
        current = self.root
        current.sem.acquire() 
        self.root_lock.release()
        
        while current is not None:
            if n < current.value:
                if current.left == None:
                    current.sem.release()
                    return False
                else:
                    current.left.sem.acquire()
                    current.sem.release()
                    current = current.left
            elif n > current.value:
                if current.right == None:
                    current.sem.release()
                    return False
                else:
                    current.right.sem.acquire()
                    current.sem.release()
                    current = current.right
            else:
                current.sem.release()
                return True
        
        return False
    
    def delete(self, n):
        self.root_lock.acquire()
        
        if self.root is None:
            self.root_lock.release()
            return False
        
        current = self.root
        current.sem.acquire()
        
        will_delete_root = (current.value == n)
        if not will_delete_root:
            self.root_lock.release()
            root_lock_held = False
        else:
            root_lock_held = True
        
        parent = None
        parent_locked = False
        
        while current.value != n:
            if n > current.value:
                next_node = current.right
            else:
                next_node = current.left
            
            if next_node is None:
                current.sem.release()
                if parent_locked:
                    parent.sem.release()
                if root_lock_held:
                    self.root_lock.release()
                return False
            
            next_node.sem.acquire()
            
            if parent_locked:
                parent.sem.release()
            
            parent = current
            parent_locked = True
            current = next_node
        
        if current.left is None and current.right is None:
            if parent is None:
                self.root = None
                self.root_lock.release()
            else:
                if parent.left == current:
                    parent.left = None
                else:
                    parent.right = None
                if root_lock_held:
                    self.root_lock.release()
            
            current.sem.release()
            if parent_locked:
                parent.sem.release()
            return True
        
        elif current.left is None or current.right is None:
            child = current.left if current.left else current.right
            
            if parent is None:
                self.root = child
                self.root_lock.release()
            else:
                if parent.left == current:
                    parent.left = child
                else:
                    parent.right = child
                if root_lock_held:
                    self.root_lock.release()
            
            current.sem.release()
            if parent_locked:
                parent.sem.release()
            return True
        
        else:
            successor_parent = current
            successor_parent_locked = False
            successor = current.right
            successor.sem.acquire()
            
            while successor.left is not None:
                next_succ = successor.left
                next_succ.sem.acquire()
                
                if successor_parent_locked:
                    successor_parent.sem.release()
                
                successor_parent = successor
                successor_parent_locked = True
                successor = next_succ
            
            current.value = successor.value
            
            if successor_parent == current:
                current.right = successor.right
            else:
                successor_parent.left = successor.right
            
            successor.sem.release()
            if successor_parent_locked:
                successor_parent.sem.release()
            current.sem.release()
            if parent_locked:
                parent.sem.release()
            if root_lock_held:
                self.root_lock.release()
            
            return True
    
    def inorder_traversal(self):
        result = []
        self._inorder_helper(self.root, result)
        return result
    
    def _inorder_helper(self, node, result):
        if node:
            self._inorder_helper(node.left, result)
            result.append(node.value)
            self._inorder_helper(node.right, result)
    
    def print_tree(self, node=None, level=0, prefix="Root: "):
        if node is None:
            if level == 0:
                node = self.root
            else:
                return
        
        if node is None:
            print("Arvore vazia")
            return
        
        print(" " * (level * 4) + prefix + str(node.value))
        if node.left or node.right:
            if node.left:
                self.print_tree(node.left, level + 1, "L--- ")
            else:
                print(" " * ((level + 1) * 4) + "L--- None")
            
            if node.right:
                self.print_tree(node.right, level + 1, "R--- ")
            else:
                print(" " * ((level + 1) * 4) + "R--- None")


def test_basic_operations():
    print("TESTE 1: Operacoes Basicas Sequenciais")
    
    bst = BinarySearchTree()
    
    print("Inserindo valores: 50, 30, 70, 20, 40, 60, 80")
    values = [50, 30, 70, 20, 40, 60, 80]
    for val in values:
        bst.insert(val)
    
    print("Estrutura da arvore:")
    bst.print_tree()
    
    print(f"In-order traversal: {bst.inorder_traversal()}")
    
    print("Testando buscas:")
    for val in [50, 20, 80, 45, 100]:
        result = bst.search(val)
        status = "Encontrado" if result else "Nao encontrado"
        print(f"Buscar {val}: {status}")
    
    print("Teste 1 concluido com sucesso.")


def test_delete_leaf():
    print("TESTE 2: Delecao de No Folha")
    
    bst = BinarySearchTree()
    values = [50, 30, 70, 20, 40, 60, 80]
    for val in values:
        bst.insert(val)
    
    print("Arvore inicial:")
    bst.print_tree()
    
    print("Deletando no folha: 20")
    bst.delete(20)
    
    print("Arvore apos delecao:")
    bst.print_tree()
    
    print(f"In-order: {bst.inorder_traversal()}")
    result = "Encontrado" if bst.search(20) else "Nao encontrado"
    print(f"Buscar 20: {result}")
    
    print("Teste 2 concluido com sucesso.")


def test_delete_one_child():
    print("TESTE 3: Delecao de No com 1 Filho")
    
    bst = BinarySearchTree()
    values = [50, 30, 70, 20, 60, 80, 55]
    for val in values:
        bst.insert(val)
    
    print("Arvore inicial:")
    bst.print_tree()
    
    print("Deletando no com 1 filho: 60 (tem apenas filho esquerdo 55)")
    bst.delete(60)
    
    print("Arvore apos delecao:")
    bst.print_tree()
    
    print(f"In-order: {bst.inorder_traversal()}")
    result_60 = "Encontrado" if bst.search(60) else "Nao encontrado"
    result_55 = "Encontrado" if bst.search(55) else "Nao encontrado"
    print(f"Buscar 60: {result_60}")
    print(f"Buscar 55: {result_55}")
    
    print("Teste 3 concluido com sucesso.")


def test_delete_two_children():
    print("TESTE 4: Delecao de No com 2 Filhos")
    
    bst = BinarySearchTree()
    values = [50, 30, 70, 20, 40, 60, 80, 55, 65]
    for val in values:
        bst.insert(val)
    
    print("Arvore inicial:")
    bst.print_tree()
    
    print("Deletando no com 2 filhos: 70")
    bst.delete(70)
    
    print("Arvore apos delecao:")
    bst.print_tree()
    
    print(f"In-order: {bst.inorder_traversal()}")
    result = "Encontrado" if bst.search(70) else "Nao encontrado"
    print(f"Buscar 70: {result}")
    
    print("Teste 4 concluido com sucesso.")


def test_delete_root():
    print("TESTE 5: Delecao da Raiz")
    
    bst = BinarySearchTree()
    values = [50, 30, 70, 20, 40, 60, 80]
    for val in values:
        bst.insert(val)
    
    print("Arvore inicial:")
    bst.print_tree()
    
    print("Deletando raiz: 50")
    bst.delete(50)
    
    print("Arvore apos delecao:")
    bst.print_tree()
    
    print(f"In-order: {bst.inorder_traversal()}")
    
    print("Teste 5 concluido com sucesso.")


def concurrent_insert_worker(bst, thread_id, values, results):
    for val in values:
        bst.insert(val)
        time.sleep(0.001)
    results[thread_id] = "OK"


def test_concurrent_inserts():
    print("TESTE 6: Insercoes Concorrentes")
   
    bst = BinarySearchTree()
    num_threads = 4
    values_per_thread = 10
    
    threads = []
    results = {}
    
    print(f"Iniciando {num_threads} threads, cada uma inserindo {values_per_thread} valores...")
    
    for i in range(num_threads):
        values = [i * 100 + j for j in range(values_per_thread)]
        thread = threading.Thread(
            target=concurrent_insert_worker,
            args=(bst, i, values, results)
        )
        threads.append(thread)
        thread.start()
    
    for thread in threads:
        thread.join()
    
    print(f"Todas as threads concluiram: {results}")
    
    inorder = bst.inorder_traversal()
    print(f"Total de nos inseridos: {len(inorder)}")
    print(f"In-order (primeiros 20): {inorder[:20]}")
    
    is_sorted = inorder == sorted(inorder)
    status = "SIM" if is_sorted else "NAO"
    print(f"Arvore esta ordenada: {status}")
    
    print("Teste 6 concluido com sucesso.")


def concurrent_search_worker(bst, thread_id, values, results):
    found_count = 0
    for val in values:
        if bst.search(val):
            found_count += 1
        time.sleep(0.001)
    results[thread_id] = found_count


def test_concurrent_searches():
    print("TESTE 7: Buscas Concorrentes")
 
    bst = BinarySearchTree()
    
    print("Populando arvore com 100 valores...")
    for i in range(100):
        bst.insert(i * 10)
    
    num_threads = 5
    threads = []
    results = {}
    
    print(f"Iniciando {num_threads} threads realizando buscas simultaneas...")
    
    for i in range(num_threads):
        values = [random.randint(0, 1000) for _ in range(20)]
        thread = threading.Thread(
            target=concurrent_search_worker,
            args=(bst, i, values, results)
        )
        threads.append(thread)
        thread.start()
    
    for thread in threads:
        thread.join()
    
    print(f"Resultados das buscas por thread: {results}")
    print(f"Total de valores encontrados: {sum(results.values())}")
    
    print("Teste 7 concluido com sucesso.")


def concurrent_mixed_worker(bst, thread_id, operations, results):
    ops_count = {"insert": 0, "search": 0, "delete": 0}
    
    for op_type, value in operations:
        if op_type == "insert":
            bst.insert(value)
            ops_count["insert"] += 1
        elif op_type == "search":
            bst.search(value)
            ops_count["search"] += 1
        elif op_type == "delete":
            bst.delete(value)
            ops_count["delete"] += 1
        time.sleep(0.001)
    
    results[thread_id] = ops_count


def test_concurrent_mixed_operations():
    print("TESTE 8: Operacoes Mistas Concorrentes")
    
    bst = BinarySearchTree()
    
    print("Populando arvore inicial...")
    for i in range(50):
        bst.insert(i * 10)
    
    num_threads = 4
    threads = []
    results = {}
    
    print(f"Iniciando {num_threads} threads com operacoes mistas...")
    
    for i in range(num_threads):
        operations = []
        for j in range(15):
            op_type = random.choice(["insert", "search", "delete"])
            value = random.randint(0, 600)
            operations.append((op_type, value))
        
        thread = threading.Thread(
            target=concurrent_mixed_worker,
            args=(bst, i, operations, results)
        )
        threads.append(thread)
        thread.start()
    
    for thread in threads:
        thread.join()
    
    print("Operacoes realizadas por thread:")
    for thread_id, ops in results.items():
        print(f"Thread {thread_id}: {ops}")
    
    inorder = bst.inorder_traversal()
    print(f"Total de nos na arvore: {len(inorder)}")
    is_sorted = inorder == sorted(inorder)
    status = "SIM" if is_sorted else "NAO"
    print(f"Arvore permanece ordenada: {status}")
    
    print("Teste 8 concluido com sucesso.")


def test_edge_cases():
    print("TESTE 9: Casos Extremos")
    
    print("Teste 9.1: Operacoes em arvore vazia")
    bst = BinarySearchTree()
    print(f"Buscar em arvore vazia: {bst.search(10)}")
    print(f"Deletar em arvore vazia: {bst.delete(10)}")
    
    print("Teste 9.2: Insercao de duplicatas")
    bst = BinarySearchTree()
    bst.insert(50)
    bst.insert(50)
    bst.insert(50)
    print(f"In-order apos 3 insercoes de 50: {bst.inorder_traversal()}")
    
    print("Teste 9.3: Delecao de elemento inexistente")
    bst = BinarySearchTree()
    for val in [50, 30, 70]:
        bst.insert(val)
    result = bst.delete(100)
    print(f"Deletar 100 (nao existe): {result}")
    print(f"Arvore permanece intacta: {bst.inorder_traversal()}")
    
    print("Teste 9 concluido com sucesso.")


def main():
    print("SUITE DE TESTES - BST THREAD-SAFE")
    print()
    
    start_time = time.time()
    
    test_basic_operations()
    print()
    test_delete_leaf()
    print()
    test_delete_one_child()
    print()
    test_delete_two_children()
    print()
    test_delete_root()
    print()
    test_concurrent_inserts()
    print()
    test_concurrent_searches()
    print()
    test_concurrent_mixed_operations()
    print()
    test_edge_cases()


if __name__ == "__main__":
    main()