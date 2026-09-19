/* 上海如静知华信息科技有限公司 https://www.zhuatech.cn/ */
package cn.zhuatech.tooling;
import org.springframework.stereotype.Component;
import java.util.*;
import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import static cn.zhuatech.tooling.Model.*;
import static cn.zhuatech.tooling.Engine.*;
/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@Component public class Domain {
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static Map<String,Object> copy(Row r){return new LinkedHashMap<>(r.data());}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static BigDecimal n(Row r,String k){return num(r.data(),k);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static BigDecimal z(Map<String,Object>d,String k){return d.containsKey(k)?num(d,k):BigDecimal.ZERO;}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static String t(Row r,String k){return txt(r.data(),k);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static List<Row> linked(Engine e,User u,String module,String key,String id){return e.all(u,module).stream().filter(r->t(r,key).equals(id)).toList();}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void unique(Engine e,User u,String module,Map<String,Object>d,String key){require(e.all(u,module).stream().noneMatch(r->t(r,key).equalsIgnoreCase(txt(d,key))),"重复的"+key);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void dates(Map<String,Object>d,String from,String to){require(!date(d,to).isBefore(date(d,from)),"结束日期不能早于开始日期");}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void change(Engine e,User u,Row row,String state,Map<String,Object>d,String note){e.save(u,row,state,d,"LINKED",note);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void edit(Engine e,User u,Row r,Map<String,Object>d){
  if(r.module().equals("readings")){require(e.ref(u,r.data(),"job","jobs").state().equals("RUNNING")&&txt(d,"job").equals(t(r,"job")),"仅进行中的任务可以修改测量值，且不得迁移任务");require(linked(e,u,"readings","job",t(r,"job")).stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"point").equals(txt(d,"point"))),"测量点编号重复");return;}
  if(r.module().equals("versions")){require(e.ref(u,r.data(),"artwork","artworks").state().equals("DRAFT"),"已送审稿件不可修改");require(txt(d,"artwork").equals(t(r,"artwork"))&&num(d,"revision").compareTo(n(r,"revision"))==0,"版本不能迁移任务或改写版本号");return;}
  for(var m:e.spec().modules())for(Row other:e.all(u,m.key()))if(!other.id().equals(r.id())&&other.data().values().stream().anyMatch(v->r.id().equals(v)))throw new Failure(409,"资料已有下游引用，请新建版本而不是改写历史");
  var fields=e.spec().module(r.module()).fields().stream().map(Field::key).toList();
  r.data().forEach((k,v)->{if(!fields.contains(k))d.put(k,v);});
  if(d.containsKey("start")&&d.containsKey("end"))dates(d,"start","end");
  if(d.containsKey("from")&&d.containsKey("to"))dates(d,"from","to");
  for(String key:List.of("serial","sku","invoice","invoiceNo","lockNo"))if(d.containsKey(key))require(e.all(u,r.module()).stream().noneMatch(x->!x.id().equals(r.id())&&t(x,key).equalsIgnoreCase(txt(d,key))),"重复唯一业务标识: "+key);
  if(d.containsKey("bonusRate"))require(num(d,"bonusRate").compareTo(num(d,"baseRate"))>=0,"达档返利率不能低于基础返利率");
  if(d.containsKey("lifeLimit"))require(num(d,"serviceEvery").compareTo(num(d,"lifeLimit"))<=0,"保养间隔不能大于寿命");
  if(d.containsKey("defects"))require(num(d,"defects").compareTo(num(d,"shots"))<=0,"不良数不能超过生产次数");
  if(d.containsKey("nps"))require(num(d,"nps").compareTo(BigDecimal.TEN)<=0&&num(d,"csat").compareTo(new BigDecimal("5"))<=0,"评价分数超出范围");
  if(d.containsKey("oxygenMin"))require(num(d,"oxygenMin").compareTo(num(d,"oxygenMax"))<0,"氧气下限须小于上限");
  if(r.module().equals("invoices"))require(e.all(u,"invoices").stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"shipment").equals(txt(d,"shipment"))),"运单已关联结算账单");
  if(r.module().equals("sales")){Row program=e.ref(u,d,"program","programs");require(program.state().equals("ACTIVE")&&!date(d,"soldAt").isBefore(date(program.data(),"start"))&&!date(d,"soldAt").isAfter(date(program.data(),"end")),"协议状态或销售日期无效");}
  if(r.module().equals("jobs")){Row instrument=e.ref(u,d,"instrument","instruments"),standard=e.ref(u,d,"standard","standards");require(!instrument.state().equals("RETIRED")&&t(instrument,"unit").equals(t(standard,"unit")),"器具状态或计量单位无效");require(!date(d,"performedAt").isAfter(LocalDate.now()),"不能记录未来校准");}
  if(r.module().equals("permits")){require(ChronoUnit.DAYS.between(date(d,"start"),date(d,"end"))<=7,"许可最长七天");require(t(e.ref(u,d,"isolation","isolations"),"location").equals(txt(d,"location")),"隔离区域不匹配");}
  if(r.module().equals("responses")){require(e.all(u,"responses").stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"survey").equals(txt(d,"survey"))&&t(x,"customer").equals(txt(d,"customer"))),"客户已存在该问卷反馈");require(t(e.ref(u,d,"customer","customers"),"consent").equals("YES"),"客户未允许反馈邀请");}
  if(r.module().equals("products")){String barcode=txt(d,"barcode");require(barcode.matches("\\d{13}"),"条码须为 EAN-13");int sum=0;for(int x=0;x<12;x++)sum+=(barcode.charAt(x)-'0')*(x%2==0?1:3);require((10-sum%10)%10==barcode.charAt(12)-'0',"EAN-13 校验位不正确");}

 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public Map<String,Object> metrics(Engine e,User u){
  var out=new LinkedHashMap<String,Object>();out.put("可用工装",e.all(u,"tools").stream().filter(r->r.state().equals("READY")).count());out.put("保养/停机工装",e.all(u,"tools").stream().filter(r->Set.of("DUE","MAINTENANCE").contains(r.state())).count());out.put("累计加工次数",e.all(u,"tools").stream().map(r->n(r,"totalShots")).reduce(BigDecimal.ZERO,BigDecimal::add));;return out;
 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void create(Engine e,User u,String module,Map<String,Object>d){switch(module){case "tools" -> {unique(e,u,module,d,"serial");require(num(d,"serviceEvery").compareTo(num(d,"lifeLimit"))<=0,"保养间隔不能大于寿命");d.put("totalShots",0);d.put("serviceShots",0);}
case "runs" -> require(num(d,"defects").compareTo(num(d,"shots"))<=0,"不良数不能大于生产次数"); default -> {} }}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public String action(Engine e,User u,Row r,String action,Map<String,Object>i,Map<String,Object>d){
  String k=r.module()+"."+action;switch(k){
case "assignments.issue" -> {
 Row tool=e.ref(u,d,"tool","tools");require(tool.state().equals("READY"),"工装不可领用");require(n(tool,"totalShots").compareTo(n(tool,"lifeLimit"))<0&&n(tool,"serviceShots").compareTo(n(tool,"serviceEvery"))<0,"工装寿命或保养计次已达上限");
 change(e,u,tool,"IN_USE",copy(tool),"工装领用");
}
case "assignments.return" -> {Row tool=e.ref(u,d,"tool","tools");String state=n(tool,"totalShots").compareTo(n(tool,"lifeLimit"))>=0?"EXHAUSTED":n(tool,"serviceShots").compareTo(n(tool,"serviceEvery"))>=0?"DUE":"READY";change(e,u,tool,state,copy(tool),"工装归还");}
case "runs.post" -> {
 Row assignment=e.ref(u,d,"assignment","assignments");require(assignment.state().equals("IN_USE"),"领用单不在使用中");Row tool=e.ref(u,assignment.data(),"tool","tools");require(tool.state().equals("IN_USE"),"工装已停用");
 BigDecimal shots=z(d,"shots"),total=n(tool,"totalShots").add(shots),service=n(tool,"serviceShots").add(shots);require(total.compareTo(n(tool,"lifeLimit"))<=0,"超过工装累计寿命");require(service.compareTo(n(tool,"serviceEvery"))<=0,"超过本次保养间隔，先归还保养");
 var td=copy(tool);td.put("totalShots",total);td.put("serviceShots",service);change(e,u,tool,tool.state(),td,"生产计次");e.ledger(u,"movements","POSTED",Map.of("tool",tool.id(),"kind","PRODUCTION","shots",shots,"run",r.id()));
}
case "maintenance.start" -> {Row tool=e.ref(u,d,"tool","tools");require(Set.of("READY","DUE").contains(tool.state()),"工装在用、报废或已有维修");change(e,u,tool,"MAINTENANCE",copy(tool),"保养停机");}
case "maintenance.complete" -> d.putAll(i);
case "maintenance.approve" -> {
 Row tool=e.ref(u,d,"tool","tools");require(tool.state().equals("MAINTENANCE"),"工装未处于维修状态");var td=copy(tool);td.put("serviceShots",0);change(e,u,tool,n(tool,"totalShots").compareTo(n(tool,"lifeLimit"))>=0?"EXHAUSTED":"READY",td,"保养验收，仅清零保养计数");
 e.ledger(u,"movements","POSTED",Map.of("tool",tool.id(),"kind","MAINTENANCE","shots",0,"maintenance",r.id()));
}
case "tools.retire" -> require(linked(e,u,"maintenance","tool",r.id()).stream().noneMatch(m->!Set.of("CLOSED","DRAFT").contains(m.state())),"还有未完成保养");
 default -> {} }return null;
 }
}
