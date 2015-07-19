package uk.co.rx14.jmclaunchlib.util

import org.junit.Test

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import static org.assertj.core.api.Assertions.*

public class TaskTest {
	static class FakeTask implements Task {

		static final AtomicInteger count = new AtomicInteger(0)

		final int weight = 1
		final List<Task> subtasks = [].asImmutable()

		final String description = "Fake task for testing"

		@Override
		void before() {
			count.getAndIncrement()
			Thread.sleep(100) //Time for the method to be entered more than once
		}

		@Override
		void after() {

		}
	}

	@Test
	void testTaskSynchronisation() {
		List<Task> tasks = []
		Random random = new Random(65734825647839L)

		1000.times {
			tasks << new FakeTask()
		}


		def pool = Executors.newFixedThreadPool(100)
		20000.times {
			pool.submit { tasks.get(random.nextInt(tasks.size())).start() }
		}

		pool.shutdown()
		assert pool.awaitTermination(5, TimeUnit.MINUTES)

		tasks.each {
			it.start() //Make sure all tasks are run
		}

		assert FakeTask.count.get() == 1000
	}
}
