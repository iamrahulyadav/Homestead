package com.spauldhaliwal.homestead;

import com.spauldhaliwal.homestead.repositories.TasksRepository;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class HomeBoardPresenterTest {

    @Test
    public void shouldPassTasksToView() {

        // given
        HomeBoardView view = new MockView();
        TasksRepository tasksRepository = new MockTasksRepository();

        // when
        HomeBoardPresenter presenter = new HomeBoardPresenter(view, tasksRepository);
        presenter.loadTasks();

        // then
        Assert.assertEquals(true, ((MockView) view).passed);

    }

    private class MockView implements HomeBoardView {

        boolean passed;

        @Override
        public void displayTasks(List<JobModel> taskList) {
            passed = true;
        }
    }

    private class MockTasksRepository implements TasksRepository {

        @Override
        public List<JobModel> getTasks() {

            return Arrays.asList(new JobModel(), new JobModel(), new JobModel());
        }
    }

}