package by.slava_borisov.nodehealthtracker.check.factory;

import by.slava_borisov.nodehealthtracker.check.checker.ServiceChecker;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import by.slava_borisov.nodehealthtracker.util.Messages;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ServiceCheckerFactory {

    private final Map<CheckType, ServiceChecker> checkers = new EnumMap<>(CheckType.class);

    public ServiceCheckerFactory(List<ServiceChecker> serviceCheckers) {
        serviceCheckers.forEach(serviceChecker ->
                checkers.put(serviceChecker.getSupportedCheckType(), serviceChecker)
        );
    }

    public ServiceChecker getChecker(CheckType checkType) {
        ServiceChecker serviceChecker = checkers.get(checkType);

        if (serviceChecker == null) {
            throw new IllegalArgumentException(
                    String.format(Messages.CHECKER_NOT_FOUND, checkType)
            );
        }

        return serviceChecker;
    }
}