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
                    //System.out.println("scot task curent inainte de for: " + task_scos_curr);
                    // il adaug in coada ca sa marchez ca se executa
                    host_q_exe.offer(task_scos_curr);

                }

                synchronized (host_q_exe) {
                    boolean preempted = false;
                    // scot taskul urmator din coada sa verific prioritatile
                    //Task nextTask = host_q.peek(); 
                    for (Iterator<Task> iterator = host_q.iterator(); iterator.hasNext() && !preempted;) {
                        //System.out.println("parcurgere coada 1");
                        Task nextTask = iterator.next();
                        //System.out.println("scot din coada: " + task_scos_curr + " si sunt " + this.getId());
                        //System.out.println("scot next prima verif: " + nextTask + " si sunt " + this.getId());
                        //System.out.println("prioritate cur: " + task_scos_curr.getPriority());
                        //System.out.println("prioritate next: " + nextTask.getPriority());
                        //System.out.println(nextTask != null && nextTask.getPriority() > task_scos_curr.getPriority());
                        if (nextTask != null && nextTask.getPriority() > task_scos_curr.getPriority()) {
                            //System.out.println("poate intri candva");
                            // daca cumva taskul curent este si preemtibil
                            if (isPreemptible(task_scos_curr)) {
                                //host_q.wait();
                                executePartialTask( nextTask, task_scos_curr);
                                iterator.remove();
                                preempted = true;
                                //break;
                            }
                            else {
                                //System.out.println("daca nu e preemty dar are prio mai mare");
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
                    // Actualizeaza timpul ramas in bucla (poate fi bazat pe un interval mic de timp)
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    task_scos_curr.setLeft(task_scos_curr.getLeft() - elapsedTime);
                    synchronized (host_q_exe) {
                    boolean preempted = false;
                    //Task nextTask = host_q.peek();
                        for (Iterator<Task> iterator = host_q.iterator(); iterator.hasNext() && !preempted;) {
                            //System.out.println("parcurgere coada 2");
                            Task nextTask = iterator.next();
                            //System.out.println("scot next a doua verif: " + nextTask + " si sunt " + this.getId());
                            if (nextTask != null && nextTask.getPriority() > task_scos_curr.getPriority()) {
                                if (isPreemptible(task_scos_curr)) {
                                    //System.out.println( "task preemtibil "+ task_scos_curr);
                                    //System.out.println("next OK: " + nextTask);
                                    // Daca e preemtibil, incepe executia noului task
                                    this.sleep(nextTask.getDuration());
                                    nextTask.finish();
                                    //System.out.println("am terminat taskul " + nextTask + " de catre " + this.getId());
                                    iterator.remove();
                                    } 
                                    
                                }
                            }
                        }

                        // Actualizeaza momentul de start pentru urmatoarea iteratie
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
                        //System.out.println("am terminat taskul " + task_scos_curr + " si sunt" + this.getId());
                        task_scos_curr.notify();
                        // System.out.println("task: " + task_scos_curr.getId() + " la mom: " + task_scos_curr.getFinish());
                        // System.out.println("Am fost executat de catre threadul: " + this.getId());
                        //System.out.println("Am asteptat: " + task_scos.getDuration());
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
            System.out.println("adaug in coada task-ul: " + task + " si sunt host: " + this.getId()) ;
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
        //synchronized(host_q) {
            //calculez timpul necesar pentru rezolvarea tuturor
            //taskurilor acumulate in coada
        for (Task element : host_q) {
            durata += element.getDuration();
            test = 1;
        }
        
        //}
        synchronized(host_q_exe) {
            for (Task task : host_q_exe) {
                //System.out.println("este in exec: " + element + " de catre: " + this.getId());
                //System.out.println("cat mai are: " + element.getLeft());
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

    // aceste doua metode erau pt partea de preemtibilitate (inca nu e OK)
    private boolean isPreemptible(Task task) {
        return task != null && task.isPreemptible();
    }

    private void executePartialTask(Task task_curr, Task next_task) throws InterruptedException {
        //System.out.println("execut partial " + task_curr);
        long remainingTime = task_curr.getLeft(); 
        long partialTime = Math.min(remainingTime, next_task.getStart());
        //System.out.println("Cat execut: " + partialTime);
        
    
        Thread.sleep(partialTime);
    
        
        task_curr.setLeft(remainingTime - partialTime);
        //System.out.println("Cat mi a mai ramas: " + task_curr.getLeft());
    
        
        synchronized (host_q) {
            host_q.offer(task_curr);
            //host_q.notify();
        }
    }

    
    // necesara la politica Shortesc Queue          
    public int lungime_cozi() {
        synchronized (lock) {
            int len = host_q.size() + host_q_exe.size();
            System.out.println("LUNGIME: " + len + " la " + this.getId());
            return host_q.size() + host_q_exe.size();
        }
    }
}
