package com.wikipedia.articles.repositories;

import com.wikipedia.articles.models.Statistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatisticRepository extends JpaRepository<Statistic, Long>{
    Optional<Statistic> findByWord(String word);
    List<Statistic> findAllByOrderByCounterDesc();
}
