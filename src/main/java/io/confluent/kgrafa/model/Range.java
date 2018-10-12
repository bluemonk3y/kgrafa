/**
 * Copyright 2018 Confluent Inc.
 * <p>
 * Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/AGPL-3.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.confluent.kgrafa.model;

import java.util.Date;

/**
 * "range": { "from": "2015-12-22T03:06:13.851Z", "to": "2015-12-22T06:48:24.137Z" },
 */
public class Range {

  private Date from = new Date(System.currentTimeMillis() - 60 * 1000);
  private Date to = new Date();

  public Range(){

  }

  public void setFrom(Date from) {
    this.from = from;
  }

  public Date getFrom() {
    return from;
  }

  public void setTo(Date to) {
    this.to = to;
  }

  public Date getTo() {
    return to;
  }

  public long getDuration() {
    return to.getTime() - from.getTime();
  }

  public long getStart() {
    return from.getTime();
  }

  public long getEnd() {
    return to.getTime();

  }
}
