/**
 *
 */
package cn.bc.workflow.activiti.delegate;

import cn.bc.core.util.JsonUtils;
import cn.bc.core.util.SpringUtils;
import cn.bc.identity.domain.ActorHistory;
import cn.bc.identity.web.SystemContextHolder;
import cn.bc.workflow.domain.ExcutionLog;
import cn.bc.workflow.service.ExcutionLogService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.el.Expression;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 流程更新模块的相关信息监听器
 *
 * @author zxr
 * @deprecated replace by cn.bc.workflow.activiti.delegate.UpdateModuleInfoOfficial4FlowListener
 */
public class updateModuleInfo4FlowListener extends ExcutionLogListener {
  protected final Log logger = LogFactory.getLog(getClass());

  protected ExcutionLogService excutionLogService;

  /**
   * service名称
   */
  private Expression serviceName;
  /**
   * service方法
   */
  private Expression serviceMethod;
  /**
   * 更新的参数 标准格式的json字符串如{"domain属性名":"属性值","name":"张三","sex":"男"...}
   * 暂不支持更新日期格式
   */
  private Expression parameter;

  /**
   * 是否执行更新方法
   */
  private Expression isExecuteUpdateMethod;

  /**
   * 更新对象的id
   */
  private Expression updateObjectId;

  /**
   * 同步日志记录
   */
  private Expression log;

  public updateModuleInfo4FlowListener() {
    excutionLogService = SpringUtils.getBean(ExcutionLogService.class);
  }

  @Override
  public void notify(DelegateExecution execution) throws Exception {
    // 判断是否执行更新方法
    String execute = isExecuteUpdateMethod.getExpressionText();
    if (execute != null) {
      if (execute.indexOf("$") != -1) {
        Object isExecute = execution.getVariable(execute.substring(
          execute.indexOf("{") + 1, execute.indexOf("}")));
        // 如果为true就执行
        if (isExecute != null) {
          if (isExecute.toString().equals("true")) {
            executeUpdateMethod(execution);
            if (log != null && execution.hasVariable(log.getExpressionText())) {
              saveExcutionLog(execution, execution.getVariable(log.getExpressionText()).toString());
            }
          }
        }
      }
    } else {
      // 如果不配置默认执行
      // 组装参数
      executeUpdateMethod(execution);
      if (log != null && execution.hasVariable(log.getExpressionText())) {
        saveExcutionLog(execution, execution.getVariable(log.getExpressionText()).toString());
      }
    }

  }

  /**
   * 更新方法的实现
   *
   * @param execution
   */
  private void executeUpdateMethod(DelegateExecution execution) {
    Map<String, Object> arguments = JsonUtils.toMap(parameter
      .getExpressionText());
    Map<String, Object> args = new HashMap<String, Object>();
    Set<String> keySet = arguments.keySet();
    for (String key : keySet) {

      Object arg = arguments.get(key);
      if (arg instanceof String) {
        String value = arg.toString();
        // 如果包含"$"号就取变量值
        if (value.indexOf("$") != -1) {
          args.put(key, execution.getVariable(value.substring(
            value.indexOf("{") + 1, value.indexOf("}"))));
        } else {
          args.put(key, arguments.get(key));
        }

      } else {
        args.put(key, arguments.get(key));
        logger.debug(arguments.get(key) + " :arguments.get(key): "
          + arguments.get(key).getClass());
      }
    }
    // 获取id
    String caseId = updateObjectId.getExpressionText();
    if (caseId.indexOf("$") != -1) {

      Object case4InfractTrafficId = execution.getVariable(caseId
        .substring(caseId.indexOf("{") + 1, caseId.indexOf("}")));
      SpringUtils.invokeBeanMethod(
        serviceName.getExpressionText(),
        serviceMethod.getExpressionText(),
        new Object[]{
          Long.valueOf(case4InfractTrafficId.toString()),
          args});
    }
  }

  private void saveExcutionLog(DelegateExecution excution, String desc) {
    // 创建同步日志
    ExcutionLog log = new ExcutionLog();
    log.setFileDate(Calendar.getInstance());
    ActorHistory h = SystemContextHolder.get().getUserHistory();
    log.setAuthorId(h.getId());
    log.setAuthorCode(h.getCode());
    log.setAuthorName(h.getName());
    log.setAssigneeId(h.getId());
    log.setAssigneeCode(h.getCode());
    log.setAssigneeName(h.getName());

    log.setListener(excution.getClass().getName());
    log.setExcutionId(excution.getId());
    log.setType(ExcutionLog.TYPE_PROCESS_SYNC_INFO);
    log.setProcessInstanceId(excution.getProcessInstanceId());

    log.setDescription(desc);
    this.excutionLogService.save(log);
  }
}
