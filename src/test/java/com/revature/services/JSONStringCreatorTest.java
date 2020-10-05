package com.revature.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revature.docutest.TestUtil;

import io.swagger.models.Model;

class JSONStringCreatorTest {
    
    private static JSONStringCreator jsonCreator;
    private static Map<String, Model> todosDefinitions;
    private static Map<String, Model> petstoreDefinitions;

    private String json;
    
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        TestUtil.initFields();
        jsonCreator = new JSONStringCreator();
        todosDefinitions = TestUtil.todos.getDefinitions();
        petstoreDefinitions = TestUtil.petstore.getDefinitions();
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {

    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testDefaultStringTodo() {
        json = jsonCreator.createDefaultJSONString("Todo", todosDefinitions);
        String todoJSON = TestUtil.TODO_JSON.replaceAll("\\s","");
        json = json.replaceAll("\\s","");
        assertEquals(todoJSON, json);
    }
    
    @Test
    void testDefaultStringPetNested() {
        String petJSON = TestUtil.PET_JSON.replaceAll("\\s","");
        json = jsonCreator.createDefaultJSONString("Pet", petstoreDefinitions);
        json = json.replaceAll("\\s","");
        System.out.println(petJSON);
        System.out.println(json);
        assertEquals(petJSON, json);
    }
    
    @Test
    void testDefaultStringNullDef() {
        assertEquals("{}", jsonCreator.createDefaultJSONString("Todo", null));
    }
    
    @Test
    void testDefaultStringNullKey() {
        assertEquals("{}", jsonCreator.createDefaultJSONString(null, todosDefinitions));
    }
    
    @Test
    void testDefaultStringNoMatch() {
        assertEquals("{}", jsonCreator.createDefaultJSONString("not a real key", todosDefinitions));
    }
    
    @Test
    void testDefaultStringEmptyKey() {
        assertEquals("{}", jsonCreator.createDefaultJSONString("", todosDefinitions));
    }

}
