package com.yeoman.mini.springmvc.service.impl;

import com.yeoman.mini.springmvc.annotation.Qualifier;
import com.yeoman.mini.springmvc.annotation.Service;
import com.yeoman.mini.springmvc.dao.TestDAO;
import com.yeoman.mini.springmvc.service.TestService;

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
 * @Date : Create in 2018/10/24
 */
@Service("service")
public class TestServiceImpl implements TestService {

    @Qualifier("testDao")
    private TestDAO testDAO;

    @Override
    public void insert() {
        System.out.println("=======================");
        testDAO.insert();
        System.out.println("=======================");
    }
}
