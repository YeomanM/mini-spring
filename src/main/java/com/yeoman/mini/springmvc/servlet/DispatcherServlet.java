package com.yeoman.mini.springmvc.servlet;

import com.yeoman.mini.springmvc.annotation.*;
import com.yeoman.mini.springmvc.controller.TestController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　  ┃
 * ┃　　　━　　  ┃
 * ┃　┳┛　┗┳  ┃
 * ┃　　　　　　  ┃
 * ┃　　　┻　　  ┃
 * ┃　　　　　    ┃
 * ┗━┓　　  ┏━┛
 * 　  ┃　　　┃神兽保佑
 * 　┃　　　┃代码无BUG！
 * 　  ┃　　　┗━━━┓
 * 　  ┃　　　　     ┣┓
 * 　  ┃　　　　　   ┏┛
 * 　  ┗┓┓┏━┓┓┏┛
 * 　    ┃┫┫　┃┫┫
 * 　    ┗┻┛　┗┻┛
 *
 * @Description :
 * ---------------------------------
 * @Author : Yeoman
 * @Date : Create in 2018/10/22
 */
@WebServlet(name = "dispatcherServlet",urlPatterns = "/",loadOnStartup = 1,
    initParams = {@WebInitParam(name = "base-package",value = "com.yeoman.mini.springmvc")})
public class DispatcherServlet extends HttpServlet {

    private String basePackage = "";
    private List<String> packageNames = new ArrayList<>();
    private Map<String,Object> instanceMap = new HashMap<>();
    private Map<String,String> nameMap = new HashMap<>();
    private Map<String,Method> urlMethodMap = new HashMap<>();
    private Map<Method,String> methodPackageMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.basePackage = config.getInitParameter("base-package");

        try{
            this.scanBasePackage(basePackage);
            this.instance(packageNames);
            this.springIOC();
            this.handlerUrlMethodMap();
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
        String uri = request.getRequestURI();
        String contentPath = request.getContextPath();
        String path = uri.replaceAll(contentPath,"");
        Method method = urlMethodMap.get(path);
        if (method != null){
            String packageName = methodPackageMap.get(method);
            String controllerName = nameMap.get(packageName);
            Object controller = instanceMap.get(controllerName);
            method.setAccessible(true);
            try {
                method.invoke(controller);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException{
        doPost(request, response);
    }

    private void scanBasePackage(String basePackage){
        URL url = this.getClass().getClassLoader().getResource(basePackage.replaceAll("\\.","/"));
        File basePackageFile = new File(url.getFile());
        System.out.println("scan:"+url.getFile());
        File[] files = basePackageFile.listFiles();
        for (File f : files){
            if (f.isDirectory()){
                scanBasePackage(basePackage+"."+f.getName());
            } else if (f.isFile()){
                packageNames.add(basePackage+"."+ f.getName().split("\\.")[0]);
            }
        }
    }

    private void instance(List<String> packageNames) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (packageNames.isEmpty()){
            return;
        }
        Class c = null;
        for (String name : packageNames){
            c = Class.forName(name);

            if (c.isAnnotationPresent(Controller.class)){
                Controller controller = (Controller) c.getAnnotation(Controller.class);
                String controllerName = controller.value();
                this.instanceMap.put(controllerName,c.newInstance());
                this.nameMap.put(name,controllerName);
            } else if (c.isAnnotationPresent(Service.class)){
                Service service = (Service) c.getAnnotation(Service.class);
                String serviceName = service.value();
                this.instanceMap.put(serviceName,c.newInstance());
                this.nameMap.put(name,serviceName);
            } else if (c.isAnnotationPresent(Repository.class)){
                Repository repository = (Repository) c.getAnnotation(Repository.class);
                String repositoryName = repository.value();
                this.instanceMap.put(repositoryName,c.newInstance());
                this.nameMap.put(name,repositoryName);
            }

        }
    }

    private void springIOC() throws IllegalAccessException {
        for (Map.Entry<String,Object> entry : instanceMap.entrySet()){
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field: fields){
                if (field.isAnnotationPresent(Qualifier.class)){
                    String name = field.getAnnotation(Qualifier.class).value();
                    field.setAccessible(true);
                    field.set(entry.getValue(),instanceMap.get(name));
                    field.setAccessible(false);
                }
            }
        }
    }

    private void handlerUrlMethodMap() throws ClassNotFoundException {
        if (packageNames.isEmpty()){
            return;
        }
        Class c = null;
        for (String name : packageNames){
            c = Class.forName(name);
            if (c.isAnnotationPresent(Controller.class)){
                String baseUrl = "";
                if (c.isAnnotationPresent(RequestMapping.class)){
                    RequestMapping mapping = (RequestMapping) c.getAnnotation(RequestMapping.class);
                    baseUrl += mapping.value();
                }
                Method[] methods = c.getMethods();
                RequestMapping mapping = null;
                String url = "";
                for (Method method : methods){
                    if(method.isAnnotationPresent(RequestMapping.class)){
                        mapping = method.getAnnotation(RequestMapping.class);
                        url = baseUrl + mapping.value();
                        urlMethodMap.put(url,method);
                        methodPackageMap.put(method,name);
                    }
                }
            }
        }
    }


}
