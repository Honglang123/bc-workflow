package cn.bc.workflow.service;

import cn.bc.core.query.condition.impl.EqualsCondition;
import cn.bc.core.service.DefaultCrudService;
import cn.bc.workflow.dao.WorkflowModuleRelationDao;
import cn.bc.workflow.domain.FlowStatus;
import cn.bc.workflow.domain.WorkflowModuleRelation;
import org.commontemplate.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 流程关系Service接口实现
 *
 * @author lbj
 * @modified dragon 2016-05-18
 */
@Service("workflowModuleRelationService")
public class WorkflowModuleRelationServiceImpl extends DefaultCrudService<WorkflowModuleRelation>
  implements WorkflowModuleRelationService {
  private WorkflowModuleRelationDao workflowModuleRelationDao;

  @Autowired
  public void setModuleRelationDao(WorkflowModuleRelationDao workflowModuleRelationDao) {
    this.workflowModuleRelationDao = workflowModuleRelationDao;
    this.setCrudDao(workflowModuleRelationDao);
  }

  @Override
  public List<Map<String, Object>> findList(Long[] mid, String mtype) {
    return this.workflowModuleRelationDao.findList(mid, mtype, null, null);
  }

  public List<Map<String, Object>> findList(Long mid, String mtype, String[] globalKeys) {
    return this.workflowModuleRelationDao.findList(mid, mtype, null,
      globalKeys);
  }

  public List<Map<String, Object>> findList(Long mid, String mtype, String key, String[] globalKeys) {
    return this.workflowModuleRelationDao.findList(mid, mtype, key, globalKeys);
  }

  public boolean hasRelation(Long mid, String mtype) {
    return this.workflowModuleRelationDao.hasRelation(mid, mtype, null);
  }

  public boolean hasRelation4Key(Long mid, String mtype, String key) {
    return this.workflowModuleRelationDao.hasRelation(mid, mtype, key);
  }

  public List<Map<String, Object>> findList(String[] mtype, String[] properties, String[] values, String[] globalKeys) {
    return this.workflowModuleRelationDao.findList(mtype, properties, values, globalKeys);
  }

  public List<WorkflowModuleRelation> findList(String pid) {
    Assert.assertNotNull(pid);
    return this.createQuery().condition(new EqualsCondition("pid", pid)).list();
  }

  @Override
  public FlowStatus getLastFlowStatus(Long mid, String mtype) {
    return this.workflowModuleRelationDao.getLastFlowStatus(mid, mtype);
  }
}