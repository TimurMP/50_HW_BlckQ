package telran.mediation;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueue<T> implements IBlkQueue<T> {
	LinkedList<T> messages = new LinkedList<>();
	int buffer;
	Lock mutex = new ReentrantLock();
	Condition producerWaitingCondition = mutex.newCondition();
	Condition consumerWaitingCondition = mutex.newCondition();



		public BlkQueue(int maxSize) {
			this.buffer = maxSize;
//		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void push(T message) {
		mutex.lock();
		try {
			if (messages.size() < buffer){
				messages.addLast(message);
			}
			else {
				while (messages.size() == buffer){
					try {
						producerWaitingCondition.await();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				messages.add(message);
				consumerWaitingCondition.signal();
			}
		} finally {
			mutex.unlock();
		}
	}

	@Override
	public T pop() {
		T res;
		mutex.lock();


		try {
			if (!messages.isEmpty()){
				res = messages.removeLast();
			}
			else {
				while (messages.isEmpty()){
					try {
						consumerWaitingCondition.await();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				res = messages.removeLast();
				producerWaitingCondition.signal();
				return res;

			}
		} finally {
			mutex.unlock();
		}
		return null;

	}
}