package org.sakuram.persmony.repository;

import org.sakuram.persmony.bean.Action;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionRepository extends JpaRepository<Action, Long> {
// Use IgnoreCase in derived methods when using EntitledIsin and input parameter is a string (not Isin object)
}
