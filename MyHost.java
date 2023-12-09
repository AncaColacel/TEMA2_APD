/* Implement this class. */

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MyHost extends Host {
    // creare coada cu taskuri pentru hosturi
    BlockingQueue<Task> host_q = new LinkedBlockingQueue<>();
    // creare coada pentru taskurile aflate in executie
    BlockingQueue<Task> host_q_exe = new LinkedBlockingQueue<>();
    BlockingQueue<Task> waiting = new LinkedBlockingQueue<>();
    List<Task> tasksToPreempt = new ArrayList<>();
    private final Object lock = new Object();
    Task task_scos_curr;
    int test;
    int notified;
    

    
    @Override
    // metoda executata de host/threaduri
    public void run() {
        try {
            while (true) {
                // scot un task din varful cozii
                synchronized (host_q) {
                    task_scos_curr = host_q.take();
                    // il adaug in coada ca sa marchez ca se executa
                    host_q_exe.offer(task_scos_curr);

                }

                synchronized (host_q_exe) {
                    boolean preempted = false;
                    // scot taskul urmator din coada sa verific prioritatile 
                    for (Iterator<Task> iterator = host_q.iterator(); iterator.hasNext() && !preempted;) {
                        Task nextTask = iterator.next();
                        if (nextTask != null && nextTask.getPriority() > task_scos_curr.getPriority()) {
                            // daca cumva taskul curent este si preemtibil
                            if (isPreemptible(task_scos_curr)) {
                                executePartialTask( nextTask, task_scos_curr);
                                iterator.remove();
                                preempted = true;
                            }
                            // daca nu e preemty dar are prioritate mai mare il execut
                            else {
                                host_q.offer(task_scos_curr);
                                task_scos_curr = nextTask;
                                break;
                            }
                        }
                    }
                }

                
                // executia acestuia este reprezentata prin asteptare 
                // pe parcursul duratei acestuia
                    
                long startTime = System.currentTimeMillis();
                while (task_scos_curr.getLeft() > 0) {
                    // actualizez timpul ramas in bucla
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    task_scos_curr.setLeft(task_scos_curr.getLeft() - elapsedTime);
                    synchronized (host_q_exe) {
                    boolean preempted = false;
                    // verific daca in timpul executiei apare un task cu prioritate mai mare
                        for (Iterator<Task> iterator = host_q.iterator(); iterator.hasNext() && !preempted;) {
                            Task nextTask = iterator.next();
                            if (nextTask != null && nextTask.getPriority() > task_scos_curr.getPriority()) {
                                if (isPreemptible(task_scos_curr)) {
                                    // daca e preemtibil, incepe executia noului task
                                    this.sleep(nextTask.getDuration());
                                    nextTask.finish();
                                    iterator.remove();
                                    } 
                                    
                                }
                            }
                        }

                        // actualizez momentul de start pentru urmatoarea iteratie
                        startTime = System.currentTimeMillis();
                        if (!host_q_exe.isEmpty()) {
                            synchronized (task_scos_curr) {
                                task_scos_curr.wait(1); 
                                
                            }
                        }
                    }
                    
                    // se marcheaza apoi taskul ca terminat
                    synchronized (task_scos_curr) {
                        task_scos_curr.finish();
                        task_scos_curr.notify();
                    }
                    // termin finalizarea taskului. il scot din coada pt exec
                    synchronized(host_q_exe) {
                        host_q_exe.take();
                        
                    }
                }
            
        } catch (InterruptedException e) {
            
        }
    }

    
    

    
    

    @Override
    public void addTask(Task task) {
        synchronized (lock) {
            // adaug task-ul in coada
            try {
                host_q.put(task);
                
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
        
        
    }

    @Override
    public int getQueueSize() {
        return host_q.size();
    }

    @Override
    public long getWorkLeft() {
        long durata = 0;
        long durata_exe = 0;
        //calculez timpul necesar pentru rezolvarea tuturor
        //taskurilor acumulate in coada
        for (Task element : host_q) {
            durata += element.getDuration();
            test = 1;
        }
        
        
        synchronized(host_q_exe) {
            for (Task task : host_q_exe) {
                if (test == 1) {
                    durata -= task.getDuration(); 
                    test = 0;
                }
                durata += task.getLeft();
                
                
            }
        }
        
        return durata;
    }


    @Override
    public void shutdown() {
        // oprire thread
        this.interrupt();
    }

    // aceste doua metode erau pt partea de preemtibilitate 
    public  boolean isPreemptible(Task task) {
        return task != null && task.isPreemptible();
    }

    // executia partiala a unui task pt preemty
    public void executePartialTask(Task task_curr, Task next_task) throws InterruptedException {
        long remainingTime = task_curr.getLeft(); 
        long partialTime = Math.min(remainingTime, next_task.getStart());
        Thread.sleep(partialTime);
        task_curr.setLeft(remainingTime - partialTime);
       synchronized (host_q) {
            host_q.offer(task_curr);
        }
    }

    
    // necesara la politica Shortesc Queue pentru lungimea cozilor         
    public int lungime_cozi() {
        synchronized (lock) {
            int len = host_q.size() + host_q_exe.size();
            System.out.println("LUNGIME: " + len + " la " + this.getId());
            return host_q.size() + host_q_exe.size();
        }
    }
}
