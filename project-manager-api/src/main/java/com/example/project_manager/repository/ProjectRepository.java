package com.example.project_manager.repository;

import com.example.project_manager.model.Project;
import com.example.project_manager.model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    @Query("SELECT COUNT(p) FROM Project p JOIN p.memberIds m WHERE m = :memberId AND p.status NOT IN :excludedStatuses")
    long countActiveProjectsByMember(@Param("memberId") Long memberId,
                                    @Param("excludedStatuses") List<ProjectStatus> excludedStatuses);

    @Query("SELECT DISTINCT m FROM Project p JOIN p.memberIds m")
    List<Long> findAllAllocatedMemberIds();
}
