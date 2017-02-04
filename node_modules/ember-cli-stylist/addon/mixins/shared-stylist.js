import Ember from 'ember';
const { keys, create } = Object; // jshint ignore:line
const { RSVP: {Promise, all, race, resolve, defer} } = Ember; // jshint ignore:line
const { inject: {service} } = Ember; // jshint ignore:line
const { computed, observer, $, run, on, typeOf, isPresent } = Ember;  // jshint ignore:line
const { defineProperty, get, set, inject, isEmpty, merge } = Ember; // jshint ignore:line
const a = Ember.A; // jshint ignore:line
const defaultBindings = [
  'width','minWidth','height','minHeight','maxHeight','fontSize',
  'fontFamily','fontWeight','fontStyle','cursor','display'
];
const sizer = size => {
  // scalar → rem value
  if(typeOf(size) === 'string' && size.match(/^(\d+)x$/)) {
    size = size.replace(/^(\d+)x$/, '$1' + 'rem');
  }
  return isNaN(Number(size)) ? size : size + 'px';
};
const dasherize = thingy => {
  return thingy ? Ember.String.dasherize(thingy) : thingy;
};

const htmlSafe = Ember.String.htmlSafe;

var SharedStylist = Ember.Mixin.create({
  _styleBindings: computed('styleBindings', function() {
    let styleBindings = this.get('styleBindings') || defaultBindings;
    styleBindings = typeOf(styleBindings) === 'string' ? styleBindings.split(',') : styleBindings;
    return styleBindings.map(sb => {
      if(sb.indexOf('::') !== -1) {
        sb = { property: sb.split('::')[0], bindTo: sb.split('::')[1] };
      } else {
        sb = { property: sb, bindTo: sb };
      }
      return sb;
    });
  }),
  _init: on('init', function() {
    const observerBindings = this.get('_styleBindings');
    observerBindings.map(item => {
      this.addObserver(item.bindTo, this._setStyle);
    });
    run.schedule('afterRender', () => {
      this._setStyle();
    });
  }),
  // Because we created the observer dynamically we must take responsibility of
  // removing the observers on exit
  _willDestroyElement: on('willDestroyElement', function() {
    const observerBindings = this.get('_styleBindings');
    observerBindings.map(item => {
      this.removeObserver(item.bindTo, this, '_setStyle');
    });
  }),
  _setStyle() {
    const styleBindings = this.get('_styleBindings');
    let styles = [];
    styleBindings.map(style => {
      const cssProp = dasherize(style.property);
      const value = this._stylist(cssProp, this.get(style.bindTo));
      if(value) {
        styles.push(`${cssProp}: ${value}`);
      }
    });
    this.set('stylist', htmlSafe(styles.join('; ')));
  },
  /**
   * Provides a per-type styler that allows for some sensible defaults
   * @param  {string} style The style property being evaluated
   * @param  {string} value The suggested value for this style property
   * @return {string}       A mildly processed/improved variant on the input
   */
  _stylist(style, value) {
    switch(style) {
    case 'font-size':
    case 'padding':
    case 'margin':
    case 'width':
    case 'height':
    case 'min-width':
    case 'max-width':
      return sizer(value);
    default:
      if(a(['undefined','null']).includes(typeOf(value))) {
        return null;
      }
      return value;
    }
  }

});

SharedStylist[Ember.NAME_KEY] = 'Shared Stylist';
export default SharedStylist;
