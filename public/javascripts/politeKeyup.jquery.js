// Inspired by: http://stackoverflow.com/a/4220182/1320693

(function ($) {
    $.fn.politeKeyup = function (politeness, handler) {
      var typingTimer;

      return this.keyup(function () {
        clearTimeout(typingTimer);
        typingTimer = setTimeout(handler, politeness);
      }).keydown(function () {
        clearTimeout(typingTimer);
      });
    };
}(jQuery));
