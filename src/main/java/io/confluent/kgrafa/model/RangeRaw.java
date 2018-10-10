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


/**
 * "range": { "from": "now-3h", "to": "now" },
 */
public class RangeRaw {

  private String from = "now-3h";
  private String now = "now";

  public RangeRaw(){
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getNow() {
    return now;
  }

  public void setNow(String now) {
    this.now = now;
  }
}
