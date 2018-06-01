/**
 *
 */
package cn.bc.workflow.web.struts2.select;

import cn.bc.BCConstants;
import cn.bc.core.query.condition.Condition;
import cn.bc.core.query.condition.ConditionUtils;
import cn.bc.core.query.condition.Direction;
import cn.bc.core.query.condition.impl.EqualsCondition;
import cn.bc.core.query.condition.impl.InCondition;
import cn.bc.core.query.condition.impl.OrderCondition;
import cn.bc.core.util.StringUtils;
import cn.bc.db.jdbc.RowMapper;
import cn.bc.db.jdbc.SqlObject;
import cn.bc.web.formater.EntityStatusFormater;
import cn.bc.web.struts2.AbstractSelectPageAction;
import cn.bc.web.ui.html.grid.Column;
import cn.bc.web.ui.html.grid.IdColumn4MapKey;
import cn.bc.web.ui.html.grid.TextColumn4MapKey;
import cn.bc.web.ui.html.page.HtmlPage;
import cn.bc.web.ui.html.page.PageOption;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.IdentityLink;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.*;

/**
 * 选择用户Action
 *
 * @author wis
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class SelectUsers4FlowAction extends
  AbstractSelectPageAction<Map<String, Object>> {
  private TaskService taskService;
  private static final long serialVersionUID = 1L;
  private boolean history;// 是否选择ActorHistory信息
  public String taskId; //任务id
  public String status = String.valueOf(BCConstants.STATUS_ENABLED) + ","
    + String.valueOf(BCConstants.STATUS_DISABLED); // 用户的状态，多个用逗号连接

  @Autowired
  public void setTaskService(TaskService taskService) {
    this.taskService = taskService;
  }

  public boolean isHistory() {
    return history;
  }

  public void setHistory(boolean history) {
    this.history = history;
  }


  @Override
  protected OrderCondition getGridDefaultOrderCondition() {
    // 默认排序方向：状态|创建时间
    return new OrderCondition("a.status_", Direction.Asc).add(
      "h.create_date", Direction.Desc);
  }

  @Override
  protected SqlObject<Map<String, Object>> getSqlObject() {
    SqlObject<Map<String, Object>> sqlObject = new SqlObject<Map<String, Object>>();

    // 构建查询语句,where和order by不要包含在sql中(要统一放到condition中)
    StringBuffer sql = new StringBuffer();
    // 是否选择ActorHistory信息
    if (this.history) {
      sql.append("select distinct h.id,a.status_,h.actor_name,h.upper_name,a.code,h.create_date ");
      sql.append("from bc_identity_actor_history h");
      sql.append(" left join bc_identity_actor a on a.id=h.actor_id ");
    } else {
      sql.append("select distinct a.id,a.status_,h.actor_name,h.upper_name,a.code,h.create_date ");
      sql.append("from bc_identity_actor_history h");
      sql.append(" left join bc_identity_actor a on a.id=h.actor_id ");
    }
    sql.append(" left join bc_identity_actor_relation ar on ar.follower_id = a.id ");
    sql.append(" left join bc_identity_actor g on ar.master_id = g.id ");
    sqlObject.setSql(sql.toString());

    // 注入参数
    sqlObject.setArgs(null);

    // 数据映射器
    sqlObject.setRowMapper(new RowMapper<Map<String, Object>>() {
      public Map<String, Object> mapRow(Object[] rs, int rowNum) {
        Map<String, Object> map = new HashMap<String, Object>();
        int i = 0;
        map.put("id", rs[i++]);
        map.put("status", rs[i++]);
        map.put("actor_name", rs[i++]);
        map.put("upper_name", rs[i++]);
        map.put("code", rs[i++]);
        return map;
      }
    });
    return sqlObject;
  }

  @Override
  protected List<Column> getGridColumns() {
    List<Column> columns = new ArrayList<Column>();
    if (this.history) {
      columns.add(new IdColumn4MapKey("h.id", "id"));
    } else {
      columns.add(new IdColumn4MapKey("a.id", "id"));
    }
    columns.add(new TextColumn4MapKey("a.status_", "status",
      getText("flow.actor.status"), 30).setSortable(true)
      .setValueFormater(new EntityStatusFormater(getBCStatuses())));
    columns.add(new TextColumn4MapKey("h.actor_name", "actor_name",
      getText("flow.user.name"), 40).setSortable(true)
      .setUseTitleFromLabel(true));
    columns.add(new TextColumn4MapKey("a.code", "code",
      getText("flow.user.code"), 40).setSortable(true)
      .setUseTitleFromLabel(true));
    columns.add(new TextColumn4MapKey("h.upper_name", "upper_name",
      getText("flow.user.department"), 40).setSortable(true)
      .setUseTitleFromLabel(true));
    return columns;
  }

  /**
   * 状态值转换列表：正常|禁用|删除|全部
   *
   * @return
   */
  protected Map<String, String> getBCStatuses() {
    Map<String, String> statuses = new LinkedHashMap<String, String>();
    statuses.put(String.valueOf(BCConstants.STATUS_ENABLED),
      getText("bc.status.enabled"));
    statuses.put(String.valueOf(BCConstants.STATUS_DISABLED),
      getText("bc.status.disabled"));
    statuses.put(String.valueOf(BCConstants.STATUS_DELETED),
      getText("bc.status.deleted"));
    statuses.put("", getText("bc.status.all"));
    return statuses;
  }

  @Override
  protected String getHtmlPageTitle() {
    return this.getText("user.title.select");
  }

  @Override
  protected String[] getGridSearchFields() {
    return new String[]{"h.actor_name", "a.code", "a.py"};
  }

  @Override
  protected PageOption getHtmlPageOption() {
    return super.getHtmlPageOption().setWidth(350).setHeight(450);
  }

  @Override
  protected String getGridRowLabelExpression() {
    return "['actor_name']";
  }

  @Override
  protected Condition getGridSpecalCondition() {
    Condition statusCondition = null;
    Condition groupCondition = null;
    Condition userCondition = null;
    Condition aTypeCondition = null;
    Condition gTypeCondition = null;
    Condition aCurrentCondition = null;

    if (status != null && status.length() > 0) {
      String[] ss = status.split(",");
      if (ss.length == 1) {
        statusCondition = new EqualsCondition("a.status_", new Integer(ss[0]));
      } else {
        statusCondition = new InCondition("a.status_",
          StringUtils.stringArray2IntegerArray(ss));
      }
    }
    List<IdentityLink> list = this.taskService.getIdentityLinksForTask(taskId);
    List<String> ulist = null;
    List<String> glist = null;
    if (null != list && list.size() > 0) {//此任务的候选用户,岗位列表
      ulist = new ArrayList<String>();
      glist = new ArrayList<String>();
      for (IdentityLink il : list) {
        if (null != il.getUserId()) {
          ulist.add(il.getUserId());
        }
        if (null != il.getGroupId()) {
          glist.add(il.getGroupId());
        }
      }
    }
    if (null != ulist && ulist.size() > 0) {
      userCondition = new InCondition("a.code", ulist);
    }
    if (null != glist && glist.size() > 0) {
      groupCondition = new InCondition("g.code", glist);
    }

    aTypeCondition = new EqualsCondition("a.type_", 4);//岗位
    gTypeCondition = new EqualsCondition("g.type_", 3);//用户

    aCurrentCondition = new EqualsCondition("h.current", true);
    return ConditionUtils.mix2AndCondition(statusCondition, aTypeCondition, gTypeCondition, aCurrentCondition
      , ConditionUtils.mix2OrCondition(userCondition, groupCondition).setAddBracket(true));
  }

  @Override
  protected void extendGridExtrasData(JSONObject json) throws JSONException {
    // 状态条件
    if (this.status != null || this.status.length() != 0) {
      json.put("status", status);
    }
    json.put("history", history);
  }

  @Override
  protected String getClickOkMethod() {
    return "bc.selectUsers4Flow.clickOk";
  }

  @Override
  protected String getHtmlPageNamespace() {
    return this.getContextPath() + "/bc-workflow";
  }

  @Override
  protected String getHtmlPageJs() {
    return this.getHtmlPageNamespace() + "/select/selectUsers.js";
  }

  @Override
  protected HtmlPage buildHtmlPage() {
    return super.buildHtmlPage().setNamespace(
      this.getHtmlPageNamespace() + "/select");
  }
}
