package com.example.demo.ui.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.ui.model.request.EmployeeDetailsRequestModel;
import com.example.demo.ui.model.response.EmplyoeeDetails;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

@RestController
@RequestMapping("/employees") //http://localhost:1024/employees
public class EmployeeController {
	


	
	private final Bucket bucket;
	
	public EmployeeController() {
		long capacity = 5;
	    Refill refill = Refill.greedy(5, Duration.ofMinutes(1));
	    Bandwidth limit = Bandwidth.classic(capacity, refill);
	    this.bucket = Bucket4j.builder().addLimit(limit).build();
	}
	
	static int eid=1;
	Map<Integer, EmplyoeeDetails> users;
	
	 
	@GetMapping(path = "/{empid}" , produces = 
		{MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<EmplyoeeDetails> getEmployee(@PathVariable int empid)
	{
		if(users.containsKey(empid))
		{
			//System.out.println("hi empid " +empid);
			if (bucket.tryConsume(1)) 
			{
				return new ResponseEntity<>(users.get(empid), HttpStatus.OK);
			}
			else
			{
				return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
			}
			
		}
		else
		{
			//System.out.println("hello empid " +empid);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		
		
	}
	
	@PostMapping(consumes = 
		{MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE}, 
		produces = 
		{MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<EmplyoeeDetails> createEmployee(@RequestBody EmployeeDetailsRequestModel userDetails)
	{
		EmplyoeeDetails returnValue  = new EmplyoeeDetails();
		
		returnValue.setEmpname(userDetails.getEmpname());
		returnValue.setSalary(userDetails.getSalary());
		returnValue.setExperience(userDetails.getExperience());
		
		
		if(users == null)
		{
			
			//System.out.println("users are null");
			users = new HashMap<>();
		}
		else
		{
			eid++;
			//System.out.println("users are not null"+eid);
		}
		returnValue.setEmpid(eid);
		users.put(eid, returnValue);
		return new ResponseEntity<EmplyoeeDetails>(returnValue, HttpStatus.OK);
		
	}
	
	@PutMapping(path = "/{empid}", consumes = 
		{MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE}, 
		produces = 
		{MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public EmplyoeeDetails updateEmployee(@PathVariable Integer empid, @RequestBody EmployeeDetailsRequestModel userDetails)
	{
		EmplyoeeDetails storedDetails = users.get(empid);
		storedDetails.setEmpname(userDetails.getEmpname());
		storedDetails.setSalary(userDetails.getSalary());
		storedDetails.setExperience(userDetails.getExperience());
		
		users.put(empid, storedDetails);
		
		return storedDetails;
		
	}
	
	@DeleteMapping(path = "/{empid}")
	public ResponseEntity<Void> deleteEmployee(@PathVariable Integer empid)
	{
		users.remove(empid);
		return ResponseEntity.noContent().build();
		
	}
	
	

}
