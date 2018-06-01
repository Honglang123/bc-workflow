/**
 *
 */
package cn.bc.workflow.todo.dao;

import java.util.List;


/**
 * 待办Dao
 *
 * @author wis
 */
public interface TodoDao {

  /**
   * 通过待办任务id判断此待办任务是否签领
   *
   * @param excludeId
   * @return
   */
  Long checkIsSign(Long excludeId);

  /**
   * 通过待办任务id用户实现签领
   *
   * @param excludeId
   * @param assignee
   */
  @Deprecated
  void doSignTask(Long excludeId, String assignee);

  /**
   * 查找用户待办任务名称
   *
   * @param account   用户账号
   * @param groupList 岗位key集合
   * @return
   */
  List<String> findTaskNames(String account, List<String> groupList);

  /**
   * 查找用户待办任务中的流程名称
   *
   * @param account   用户账号
   * @param groupList 岗位key集合
   * @return
   */
  List<String> findProcessNames(String account, List<String> groupList);

  /**
   * 查找待办任务名称
   *
   * @return
   */
  List<String> findTaskNames();

  /**
   * 查找待办任务中的流程名称
   *
   * @return
   */
  List<String> findProcessNames();


}