package com.arop.bliss_app;

import com.arop.bliss_app.apiObjects.Question;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void question_addVote() throws Exception {
        Question qt = new Question(1,"","","question","2015");
        qt.addQuestionChoice("choice1",0);
        qt.addQuestionChoice("choice2",0);

        assertEquals(0,qt.getChoices().get(0).getVotes());
        assertEquals(0,qt.getChoices().get(1).getVotes());

        qt.addVote("choice1");

        assertEquals(1,qt.getChoices().get(0).getVotes());
        assertEquals(0,qt.getChoices().get(1).getVotes());

        qt.addVote("choice2");
        assertEquals(1,qt.getChoices().get(0).getVotes());
        assertEquals(1,qt.getChoices().get(1).getVotes());
    }

    @Test
    public void equalQuestions() throws Exception {
        Question qt = new Question(1,"","","question","2015");
        Question qt1 = new Question(2,"","","question","2015");
        Question qt2 = new Question(1,"","","question","2015");

        assertEquals(qt,qt);
        assertNotEquals(qt,qt1);
        assertEquals(qt,qt2);
    }
}