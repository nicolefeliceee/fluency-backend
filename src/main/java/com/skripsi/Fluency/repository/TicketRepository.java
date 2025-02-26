package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.ProjectHeader;
import com.skripsi.Fluency.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    Ticket findByProjectHeader(ProjectHeader projectHeader);
    List<Ticket> findAllByStatusIgnoreCaseOrderByIdAsc(String status);
}
