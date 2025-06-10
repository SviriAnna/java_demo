package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;

@Service
@RequiredArgsConstructor
public class DataSourceErrorLogService {

    private final DataSourceErrorLogRepository dataSourceErrorLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(DataSourceErrorLog log) {
        dataSourceErrorLogRepository.save(log);
    }
}