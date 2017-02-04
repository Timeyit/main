import Ember from 'ember';
const { get, set, debug } = Ember; // jshint ignore:line


export function classy(params/*, hash*/) {
  return params ? Ember.String.htmlSafe(params.filter(f => f && get(this,  f)).join(' ')) : '';
}

export default Ember.Helper.helper(classy);
