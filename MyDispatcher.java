/* Implement this class. */

import java.util.List;

public class MyDispatcher extends Dispatcher {
    int first_task;
    int index_host;
    int len_hosts;


    public MyDispatcher(SchedulingAlgorithm algorithm, List<Host> hosts) {
        super(algorithm, hosts);
        first_task = 0;
        index_host = 0;
        len_hosts = hosts.size();
        

    }

    @Override
    public synchronized void addTask(Task task) {
        // System.out.println(task);
        // System.out.println(task);
        // cazul in care se alege politica ROUND RABIN
        if (algorithm.equals(SchedulingAlgorithm.ROUND_ROBIN)) {
            // SINCRONIZARE, IMI REZOLVA TESTUL 2 DAR DE CE??? (nu e serial aici?)
            //synchronized(this) {
                // pornesc cu primul host cu ID-ul 0 pentru primul task
                if (first_task == 0) {
                    first_task = 1;
                    hosts.get(index_host).addTask(task);
                }
                // daca nu mai sunt la primul task folosesc regula (host_anterior + 1) % nr_host
                else {
                    // aplic formula specifica acestei politici
                    //System.out.println("SUNT HOSTUL: " + hosts.get((index_host + 1) % len_hosts).getId());
                    //System.out.println("ID din D: " + hosts.get((index_host + 1) % len_hosts).getId());
                    hosts.get((index_host + 1) % len_hosts).addTask(task);
                    index_host = index_host + 1;
                      
                    
                }
            //}
            
        }
        // cazul in care se alege politica Shortesc Queue
        if (algorithm.equals(SchedulingAlgorithm.SHORTEST_QUEUE)) {
            //synchronized(this) {
                MyHost minQueueSumHost = (MyHost)hosts.get(0);
                int minQueueSum = minQueueSumHost.lungime_cozi();
                // parcurg fiecare host din coada ca sa l gasesc pe cel cu suma 
                // elementelor din cozi mai mica
                for (Host host : hosts) {
                    MyHost h = (MyHost) host;
                    int currentQueueSum = h.lungime_cozi();
                    if (currentQueueSum < minQueueSum) {
                        minQueueSum = currentQueueSum;
                        minQueueSumHost = h;
                    }
                } 
                // adaug taskul hostului selectat
                minQueueSumHost.addTask(task);
            //}
            
        }

        // cazul in care se alege politic SITA
        if (algorithm.equals(SchedulingAlgorithm.SIZE_INTERVAL_TASK_ASSIGNMENT)) {
            // daca taskul este scurt il asignez hostului 0
            if (task.getType().equals(TaskType.SHORT)) {
                hosts.get(0).addTask(task);
            }
            // daca taskul este mediu il asignez hostului 1
            else if (task.getType().equals(TaskType.MEDIUM)) {
                hosts.get(1).addTask(task);
            }
            // daca taskul este lung il asignez hostului 2
            else if (task.getType().equals(TaskType.LONG)) {
                hosts.get(2).addTask(task);
            }
        }

        // cazul in care se alege politica LWL
        if (algorithm.equals(SchedulingAlgorithm.LEAST_WORK_LEFT)) {
            //synchronized(this) {
                MyHost minLeftHost = (MyHost)hosts.get(0);
                long minLeft = minLeftHost.getWorkLeft();
                // parcurg fiecare host din coada ca sa l gasesc pe cel cu durata totala de
                // calcule de executat minima
                for (Host host : hosts) {
                    MyHost h = (MyHost) host;
                    long currentQueueLeft = h.getWorkLeft();
                    if (currentQueueLeft < minLeft) {
                        minLeft = currentQueueLeft;
                        minLeftHost = h;
                    }
                } 
                // adaug taskul hostului selectat
                minLeftHost.addTask(task);
            //}
        }
    }  
}
