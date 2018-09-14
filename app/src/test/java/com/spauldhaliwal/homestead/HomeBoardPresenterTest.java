package com.spauldhaliwal.homestead;

import com.spauldhaliwal.homestead.repositories.TasksRepository;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomeBoardPresenterTest {

    @Test
    public void shouldPassTasksToView() {

        // given
        HomeBoardView view = new MockView();
        TasksRepository tasksRepository = new MockTasksRepository(true);

        // when
        HomeBoardPresenter presenter = new HomeBoardPresenter(view, tasksRepository);
        presenter.loadTasks();

        // then
        Assert.assertEquals(true, ((MockView) view).displayTasksWithTasksCalled);

    }

    @Test
    public void shouldHandleNoTasksFound() {

        // given
        HomeBoardView view = new MockView();
        TasksRepository tasksRepository = new MockTasksRepository(false);

        // when
        HomeBoardPresenter presenter = new HomeBoardPresenter(view, tasksRepository);
        presenter.loadTasks();

        // then
        Assert.assertEquals(true, ((MockView) view).displayTasksWithNoTasksCalled);
    }

    private class MockView implements HomeBoardView {

        boolean displayTasksWithTasksCalled;
        boolean displayTasksWithNoTasksCalled;

        @Override
        public void displayTasks(List<JobModel> taskList) {
            if (taskList.size() == 3) displayTasksWithTasksCalled = true;
        }

        @Override
        public void displayNoTasks() {
            displayTasksWithNoTasksCalled = true;
        }
    }

    private class MockTasksRepository implements TasksRepository {

        private boolean returnSomeTasks;

        MockTasksRepository(boolean returnSomeTasks) {
            this.returnSomeTasks = returnSomeTasks;
        }

        @Override
        public List<JobModel> getTasks() {

            if (returnSomeTasks) {
                return Arrays.asList(new JobModel(), new JobModel(), new JobModel());
            } else {
                return Collections.emptyList();
            }

        }
    }

}