package uk.co.rx14.jmclaunchlib.util

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.ToString

import java.util.concurrent.atomic.AtomicBoolean

import static uk.co.rx14.jmclaunchlib.Constants.TaskLogger
import static uk.co.rx14.jmclaunchlib.Constants.executor

/**
 * A task that can be competed, has a relative weight and subtasks.
 * Used for tracking progress.
 * <p>
 * Total weight of this task is this task's weight + subtasks weight. Subtasks are counted recursively.
 */
@CompileStatic
@ToString(includePackage = false, includeNames = true)
@Canonical
trait Task {


	abstract List<Task> getSubtasks()

	abstract int getWeight()

	/**
	 * Description should be in in imperative present tense, e.g.
	 * "Download example.txt" instead of "Downloading example.txt"
	 *
	 * @return the description of this task
	 */
	abstract String getDescription()

	abstract void before()

	abstract void after()

	private AtomicBoolean _started = new AtomicBoolean()
	private boolean _done

	private float _startTime

	void start() {
		if (!_started.compareAndSet(false, true)) {
			synchronized (this) {
				while (!_done) {
					this.wait(10000)
				} //Wait for task to finish
			}
			return
		} //Only one thread may progress past here

		TaskLogger.trace "[${Thread.currentThread().name}] ${getClass()}::before enter"
		_startTime = System.nanoTime()
		before()
		TaskLogger.trace "[${Thread.currentThread().name}] ${getClass()}::before exit"

		TaskLogger.trace "[${Thread.currentThread().name}] ${getClass()} starting in parallel: $subtasks"
		subtasks.collect { Task task -> executor.submit { task.start() } }.reverse()*.get()

		TaskLogger.trace "[${Thread.currentThread().name}] ${getClass()}::after enter"
		after()
		def time = System.nanoTime() - _startTime
		TaskLogger.debug "$description: finished in ${time / 1000000000}s"

		_done = true
		synchronized (this) { this.notifyAll() }
	}

	int getTotalWeight() {
		weight + (int)subtasks.totalWeight.sum(0)
	}

	int getCompletedWeight() {
		(_done ? weight : 0) + (int)subtasks.completedWeight.sum(0)
	}

	double getCompletedPercentage() {
		completedWeight / totalWeight * 100d
	}

	/**
	 * @return a list of done tasks, earliest started task first
	 */
	List<Task> getDoneTasks() {
		def tasks = subtasks.doneTasks.flatten() as List<Task>
		if (_done) {
			tasks += this
		}
		tasks.sort { Task a, Task b -> a.startTime<=>b.startTime }
	}

	/**
	 * @return a list of currently executing tasks, latest started task first
	 */
	List<Task> getCurrentTasks() {
		def tasks = subtasks.currentTasks.flatten() as List<Task>
		if (_started.get() && !_done) {
			tasks += this
		}
		tasks.sort { Task a, Task b -> b.startTime<=>a.startTime }
	}

	/**
	 * @return a list of tasks waiting to be executed, ordered in some weird flat representation of the task tree
	 */
	List<Task> getRemainingTasks() {
		def tasks = subtasks.remainingTasks.flatten() as List<Task>
		if (!_started.get() && !_done) {
			tasks += this
		}
		tasks as List<Task>
	}

	boolean isStarted() { _started.get() }

	boolean isDone() { _done }

	float getStartTime() { _startTime }
}
