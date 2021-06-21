package com.example.demo.ui.controller;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Component
public class requestThrottleFilter  implements Filter{

    private int MAX_REQUESTS_PER_MINUTE = 5; //or whatever you want it to be

    //private LoadingCache<String, Integer> requestCountsPerIpAddress;
    private LoadingCache<String, Integer> requestCountsPerIpAddress;
    public requestThrottleFilter(){
        super();
        requestCountsPerIpAddress = CacheBuilder.newBuilder().
                expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
                	public Integer load(String key) {
                        return 0;
            }
        });
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
    	//URL url1=new URL("http://localhost:1024/employees/");
    	//URL url1=new URL("http://localhost:1024/employees//^[0-9]+$/");
    	HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String clientIpAddress = getClientIP((HttpServletRequest) servletRequest);
        StringBuffer requestURL = httpServletRequest.getRequestURL();
        boolean isGet = "GET".equals(httpServletRequest.getMethod());
        
        Pattern pattern = Pattern.compile("http://localhost:1024/employees/\\d+");
        Matcher matcher = pattern.matcher(requestURL.toString());
        boolean matchFound = matcher.find();
        
        //System.out.println(matchFound);
        //System.out.println((requestURL.toString()).matches(url1.toString()));
        
        if(isMaximumRequestsPerSecondExceeded(clientIpAddress,isGet) && matchFound){
          httpServletResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
          httpServletResponse.getWriter().write("Too many requests please try again after some time");
          return;
         }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isMaximumRequestsPerSecondExceeded(String clientIpAddress, boolean isGet){
        int requests = 0;
        try {
            requests = requestCountsPerIpAddress.get(clientIpAddress);
            if(requests >= MAX_REQUESTS_PER_MINUTE){
            	requestCountsPerIpAddress.put(clientIpAddress, requests);
                return true;
             }
        } catch (ExecutionException e) {
            requests = 0; 
        }
        if(isGet)
        	requests++;

        requestCountsPerIpAddress.put(clientIpAddress, requests);
        return false;
    }

    public String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null){
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0]; 
    }

    @Override
    public void destroy() {

    }

}
