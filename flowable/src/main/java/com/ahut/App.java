package com.ahut;

import org.flowable.engine.*;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Hello world!
 *
 */
public class App 
{
    private static ProcessEngine processEngine;
    public static void main( String[] args )
    {
        App app = new App();
        app.validEngine();
        app.init();
    }

    /**
     * 实例化一个ProcessEngine实例。这是一个线程安全的对象，您通常只需在应用程序中实例化一次
     * ProcessEngine是从ProcessEngineConfiguration实例创建的，它允许您配置和调整流程引擎的设置
     * ProcessEngineConfiguration是使用配置 XML 文件创建的，但是（就像我们在这里所做的那样）您也可以通过编程方式创建它。
     * ProcessEngineConfiguration需要的最低配置是与数据库的 JDBC 连接
     * @return
     */
    private   ProcessEngine processEngine(){
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1")
                .setJdbcUsername("sa")
                .setJdbcPassword("")
                .setJdbcDriver("org.h2.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        ProcessEngine processEngine = cfg.buildProcessEngine();
        return processEngine;
    }

    public RepositoryService repositoryService(){
        ProcessEngine processEngine = processEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        return repositoryService;
    }

    public  Deployment deployment(){
        RepositoryService repositoryService = repositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("holiday-request.bpmn20.xml")
                .deploy();
        return deployment;
    }
    public  void validEngine(){
        RepositoryService repositoryService = repositoryService();
        Deployment deployment = deployment();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        System.out.println("Found process definition : " + processDefinition.getName());
    }

    public void init(){
        Scanner scanner= new Scanner(System.in);

        System.out.println("Who are you?");
        String employee = scanner.nextLine();

        System.out.println("How many holidays do you want to request?");
        Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());

        System.out.println("Why do you need them?");
        String description = scanner.nextLine();
        start(employee,nrOfHolidays,description);
    }
    public void start(String employee,int nrOfHolidays,String description){
        ProcessEngine processEngine = processEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("employee", employee);
        variables.put("nrOfHolidays", nrOfHolidays);
        variables.put("description", description);
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey("holidayRequest", variables);
    }

    /**
     * 查询任务
     */
    public void queryTask(){
        ProcessEngine processEngine = processEngine();
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        System.out.println("You have " + tasks.size() + " tasks:");
        for (int i=0; i<tasks.size(); i++) {
            System.out.println((i+1) + ") " + tasks.get(i).getName());
        }
    }
}
