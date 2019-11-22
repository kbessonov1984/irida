"use strict";

/**
 * @param {string[]} keys the translation keys required for the entry
 * @param {string} entry the name of the entry
 * @returns {string} the generated html
 */
const template = (keys, entry) => `
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
  <body>
    <script id="${entry.replace("/", "-")}-translations" th:inline="javascript" th:fragment="i18n">
      window.translations = window.translations || [];
      window.translations.push({
        ${keys.map(key => `"${key}": /*[[#{${key}}]]*/ ""`)}
      });
    </script>
  </body>
</html>
`;

/**
 * @param {string} request the path to the js file being requested
 * @returns {boolean} request is valid and is local
 */
const isValidLocalRequest = (request) => {
  return (
    typeof request !== "undefined" &&
    request.match(/src\/main\/webapp\/resources\/js/)
  );
}

class i18nThymeleafWebpackPlugin {
  constructor(options) {
    this.options = options || {};
    this.functionName = this.options.functionName || "i18n";
  }

  /**
   * @param {compiler} compiler the compiler instance
   * @returns {void}
   */
  apply(compiler) {
    let i18nsByRequests = {};

    /**
     * @param {ChunkGroup} chunkGroup the ChunkGroup to get translations keys from
     * @return {Set<string>} a set of the translations keys required by the chunkGroup
     */
    const getKeysByChunkGroup = (chunkGroup) => {
      let keys = new Set();

      for (const chunk of chunkGroup.chunks) {
        for (const issuer of chunk.modulesIterable) {
          if (isValidLocalRequest(issuer.userRequest) && i18nsByRequests[issuer.userRequest]) {
            keys = new Set([...keys, ...i18nsByRequests[issuer.userRequest]]);
          }
        }
      }

      const childKeys = chunkGroup
        .getChildren()
        .map(child => [...getKeysByChunkGroup(child)]);

      return new Set([...keys, ...childKeys.flat()]);
    }

    /*
    This gathers all the translation keys for each file in a entry.
     */
    compiler.hooks.normalModuleFactory.tap(
      "i18nThymeleafWebpackPlugin",
      factory => {
        factory.hooks.parser
          .for("javascript/auto")
          .tap("i18nThymeleafWebpackPlugin", parser => {
            parser.hooks.call
              .for(this.functionName)
              .tap("i18nThymeleafWebpackPlugin", expr => {
                /*
                Make sure an argument was passed to the function.
                 */
                if (expr.arguments.length) {
                  const key = expr.arguments[0].value;
                  i18nsByRequests[parser.state.module.userRequest] =
                    i18nsByRequests[parser.state.module.userRequest] ||
                    new Set();
                  i18nsByRequests[parser.state.module.userRequest].add(key);
                }
              });
          });
      }
    );

    compiler.hooks.emit.tapAsync(
      "i18nThymeleafWebpackPlugin",
      (compilation, callback) => {
        for (const [
          entrypointName,
          entrypoint
        ] of compilation.entrypoints.entries()) {
          const keys = [...getKeysByChunkGroup(entrypoint)];

          if (keys.length) {
            /*
            This adds a file for translations for webpack to write to the file system.
             */
            const html = template(keys, entrypointName);
            compilation.assets[`i18n/${entrypointName}.html`] = {
              source: () => html,
              size: () => html.length
            };
          }
        }
        callback();
      }
    );
  }
}

module.exports = i18nThymeleafWebpackPlugin;
