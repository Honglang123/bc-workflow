/**
 *
 */
package cn.bc.workflow.web.struts2;

import cn.bc.core.query.condition.Condition;
import cn.bc.core.query.condition.Direction;
import cn.bc.core.query.condition.impl.EqualsCondition;
import cn.bc.core.query.condition.impl.OrderCondition;
import cn.bc.db.jdbc.RowMapper;
import cn.bc.db.jdbc.SqlObject;
import cn.bc.web.formater.AbstractFormater;
import cn.bc.web.formater.CalendarFormater;
import cn.bc.web.struts2.ViewAction;
import cn.bc.web.ui.html.grid.Column;
import cn.bc.web.ui.html.grid.IdColumn4MapKey;
import cn.bc.web.ui.html.grid.TextColumn4MapKey;
import cn.bc.web.ui.html.page.PageOption;
import cn.bc.web.ui.html.toolbar.Toolbar;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流转日志视图Action
 *
 * @author dragon
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class ExcutionLogsAction extends ViewAction<Map<String, Object>> {
  private static final long serialVersionUID = 1L;
  public String pid;// 流程实例id

  @Override
  protected String getFormActionName() {
    return "excutionLog";
  }

  @Override
  public boolean isReadonly() {
    return false;
  }

  @Override
  protected OrderCondition getGridDefaultOrderCondition() {
    return new OrderCondition("l.file_date", Direction.Asc);
  }

  @Override
  protected SqlObject<Map<String, Object>> getSqlObject() {
    SqlObject<Map<String, Object>> sqlObject = new SqlObject<Map<String, Object>>();

    // 构建查询语句,where和order by不要包含在sql中(要统一放到condition中)
    StringBuffer sql = new StringBuffer();
    sql.append("select l.id id, l.type_, l.listenter,l.eid,l.pid");
    sql.append(",tid tid,l.ecode tkey,l.ename tname, l.formkey form");
    sql.append(",l.assignee_name assignee_name,l.author_id author_id, l.author_code author_code, l.author_name author_name, l.file_date file_date,l.desc_");
    sql.append(" from bc_wf_excution_log l");
    sqlObject.setSql(sql.toString());

    // 注入参数
    sqlObject.setArgs(null);

    // 数据映射器
    sqlObject.setRowMapper(new RowMapper<Map<String, Object>>() {
      public Map<String, Object> mapRow(Object[] rs, int rowNum) {
        Map<String, Object> map = new HashMap<String, Object>();
        int i = 0;
        map.put("id", rs[i++]);
        map.put("type", rs[i++]);
        map.put("listenter", rs[i++]);
        map.put("eid", rs[i++]);
        map.put("pid", rs[i++]);

        map.put("task_id", rs[i++]);
        map.put("ecode", rs[i++]);
        map.put("ename", rs[i++]);
        map.put("formkey", rs[i++]);

        map.put("assignee_name", rs[i++]);
        map.put("author_id", rs[i++]);
        map.put("author_code", rs[i++]);
        map.put("author_name", rs[i++]);
        map.put("file_date", rs[i++]);
        map.put("desc", rs[i++]);
        return map;
      }
    });
    return sqlObject;
  }

  @Override
  protected List<Column> getGridColumns() {
    List<Column> columns = new ArrayList<Column>();
    columns.add(new IdColumn4MapKey("l.id_", "id"));
    if (this.pid == null || this.pid.length() == 0) {
      columns.add(new TextColumn4MapKey("l.eid", "eid",
        getText("flow.log.eid"), 80).setSortable(true));
      columns.add(new TextColumn4MapKey("l.pid", "pid",
        getText("flow.log.pid"), 80).setSortable(true));
    }

    columns.add(new TextColumn4MapKey("l.file_date", "file_date",
      getText("flow.log.file_date"), 145).setSortable(true)
      .setValueFormater(new CalendarFormater("yyyy-MM-dd HH:mm:ss")));
    columns.add(new TextColumn4MapKey("l.author_name", "author_name",
      getText("flow.log.author_name"), 70).setSortable(true));
    columns.add(new TextColumn4MapKey("l.type_", "type",
      getText("flow.log.type"), 80).setSortable(true)
      .setUseTitleFromLabel(true)
      .setValueFormater(new AbstractFormater<String>() {
        @Override
        public String format(Object context, Object value) {
          return getText("flow.log.type." + (String) value);
        }
      }));
    columns.add(new TextColumn4MapKey("l.ename", "ename",
      getText("flow.log.ename"), 200).setSortable(true)
      .setUseTitleFromLabel(true));
    columns.add(new TextColumn4MapKey("t.assignee_name", "assignee_name",
      getText("flow.log.assignee_name"), 150).setSortable(true)
      .setUseTitleFromLabel(true));
    columns.add(new TextColumn4MapKey("l.ecode", "ecode",
      getText("flow.log.ecode"), 180).setSortable(true)
      .setUseTitleFromLabel(true));
    columns.add(new TextColumn4MapKey("l.formkey", "formkey",
      getText("flow.log.formkey")).setSortable(true)
      .setUseTitleFromLabel(true));
    columns.add(new TextColumn4MapKey("l.listenter", "listenter",
      getText("flow.log.listenter"), 90).setSortable(true)
      .setUseTitleFromLabel(true));
    columns.add(new TextColumn4MapKey("l.author_code", "author_code",
      getText("flow.log.author_code"), 80).setSortable(true));
    columns.add(new TextColumn4MapKey("l.desc_", "desc",
      getText("flow.log.desc"), 80).setUseTitleFromLabel(true));
    return columns;
  }

  @Override
  protected String[] getGridSearchFields() {
    return new String[]{"l.ecode", "l.author_name", "l.ename",
      "l.pid", "l.type_"};
  }

  @Override
  protected PageOption getHtmlPageOption() {
    return super.getHtmlPageOption().setWidth(720).setMinWidth(400)
      .setHeight(400).setMinHeight(200);
  }

  @Override
  protected String getGridRowLabelExpression() {
    return "['id']";
  }

  @Override
  protected Toolbar getHtmlPageToolbar() {
    Toolbar tb = new Toolbar();
    tb.addButton(Toolbar.getDefaultEmptyToolbarButton());
    // 搜索按钮
    tb.addButton(getDefaultSearchToolbarButton());
    return tb;
  }

  @Override
  protected Condition getGridSpecalCondition() {
    if (this.pid != null && this.pid.length() > 0) {
      return new EqualsCondition("l.pid", this.pid);
    } else {
      return null;
    }
  }

  @Override
  protected void extendGridExtrasData(JSONObject json) throws JSONException {
    if (this.pid != null && this.pid.length() > 0) {
      json.put("pid", this.pid);
    }
  }

  @Override
  protected String getGridDblRowMethod() {
    return null;
  }

  @Override
  protected String getHtmlPageTitle() {
    return this.getText("flow.log.title");
  }

}