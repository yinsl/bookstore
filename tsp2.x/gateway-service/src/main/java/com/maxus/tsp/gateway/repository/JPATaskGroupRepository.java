package com.maxus.tsp.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maxus.tsp.gateway.entity.TaskGroup;


@Repository
public interface JPATaskGroupRepository extends JpaRepository<TaskGroup, Long>{
	
	public TaskGroup findByGroupNameAndGroupNum(String groupName,int groupNum);

}
