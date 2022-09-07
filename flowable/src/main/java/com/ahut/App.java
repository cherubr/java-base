package com.ahut;

import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
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
    private static volatile ProcessEngine processEngine;
    public static void main( String[] args )
    {
        App app = new App();
        app.validEngine();
        ProcessInstance processInstance = app.init();
        app.queryTask();
        app.submit();
        app.queryHistory(processInstance);
    }

    /**
     * 实例化一个ProcessEngine实例。这是一个线程安全的对象，您通常只需在应用程序中实例化一次
     * ProcessEngine是从ProcessEngineConfiguration实例创建的，它允许您配置和调整流程引擎的设置
     * ProcessEngineConfiguration是使用配置 XML 文件创建的，但是（就像我们在这里所做的那样）您也可以通过编程方式创建它。
     * ProcessEngineConfiguration需要的最低配置是与数据库的 JDBC 连接
     * @return
     */
   static {
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1")
                .setJdbcUsername("sa")
                .setJdbcPassword("")
                .setJdbcDriver("org.h2.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        if(processEngine == null){
            processEngine = cfg.buildProcessEngine();
        }
    }

    public RepositoryService repositoryService(){

        RepositoryService repositoryService = processEngine.getRepositoryService();
        return repositoryService;
    }

    /**
     * 流程引擎会将 XML 文件存储在数据库中，以便在需要时随时检索
     * 流程定义被解析为一个内部的、可执行的对象模型，因此流程实例可以从它开始
     * @return
     */
    public  Deployment deployment(){
        RepositoryService repositoryService = repositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("holiday-request.bpmn20.xml")
                .deploy();
        return deployment;
    }

    /**
     * API 查询来验证引擎是否知道流程定义（并了解一些关于 API 的知识）。这是通过RepositoryService创建一个新的ProcessDefinitionQuery对象来完成的
     */
    public  void validEngine(){
        RepositoryService repositoryService = repositoryService();
        Deployment deployment = deployment();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        System.out.println("Found process definition : " + processDefinition.getName());
    }

    /**
     * 初始化任务
     * 我们可以通过RuntimeService启动一个流程实例。收集到的数据作为java.util.Map实例传递，其中键是稍后将用于检索变量的标识符。流程实例使用key启动
     */
    public ProcessInstance init(){
        Scanner scanner= new Scanner(System.in);

        System.out.println("Who are you?");
        String employee = scanner.nextLine();

        System.out.println("How many holidays do you want to request?");
        Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());

        System.out.println("Why do you need them?");
        String description = scanner.nextLine();
        ProcessInstance processInstance = start(employee, nrOfHolidays, description);
        return processInstance;
    }

    /**
     * 我们可以通过RuntimeService启动一个流程实例。收集到的数据作为java.util.Map实例传递，其中键是稍后将用于检索变量的标识符。流程实例使用key启动
     * @param employee
     * @param nrOfHolidays
     * @param description
     */
    public ProcessInstance start(String employee,int nrOfHolidays,String description){
        RuntimeService runtimeService = processEngine.getRuntimeService();

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("employee", employee);
        variables.put("nrOfHolidays", nrOfHolidays);
        variables.put("description", description);
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey("holidayRequest", variables);
        return processInstance;
    }

    /**
     * 查询任务
     */
    public void queryTask(){
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        System.out.println("You have " + tasks.size() + " tasks:");
        for (int i=0; i<tasks.size(); i++) {
            System.out.println((i+1) + ") " + tasks.get(i).getName());
        }
    }

    /**
     * 提交任务,完成任务
     */
    public void submit(){
        Scanner scanner= new Scanner(System.in);
        System.out.println("Which task would you like to complete?");
        int taskIndex = Integer.valueOf(scanner.nextLine());
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        Task task = tasks.get(taskIndex - 1);
        Map<String, Object> processVariables = taskService.getVariables(task.getId());
        System.out.println(processVariables.get("employee") + " wants " +
                processVariables.get("nrOfHolidays") + " of holidays. Do you approve this?");
        boolean approved = scanner.nextLine().toLowerCase().equals("y");
        Map variables = new HashMap<String, Object>();
        variables.put("approved", approved);
        taskService.complete(task.getId(), variables);
    }

    public void queryHistory(ProcessInstance processInstance){
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricActivityInstance> activities =
                historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstance.getId())
                        .finished()
                        .orderByHistoricActivityInstanceEndTime().asc()
                        .list();

        for (HistoricActivityInstance activity : activities) {
            System.out.println(activity.getActivityId() + " took "
                    + activity.getDurationInMillis() + " milliseconds");
        }
    }
}
