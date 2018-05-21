export function jfDisableFeature(ArtifactoryFeatures, $timeout) {
  return {
    restrict: 'A',
    link: function ($scope, $element, $attrs) {
      // console.log($scope, $element, $attrs);
      let feature = $attrs.jfDisableFeature;
      let currentLicense = ArtifactoryFeatures.getCurrentLicense();
      if (!feature) return;
      if (ArtifactoryFeatures.isHidden(feature)) {
        $($element).hide();
      }
      else if (ArtifactoryFeatures.isDisabled(feature)) {
        if (currentLicense === "OSS" || currentLicense === "ConanCE") {
          if (feature === 'publishedmodule') {
           //$($element).tooltipster('destroy');
            $($element).addClass('publishedModuleDisabled');
            return;
          }
          $timeout(() => {
              $($element).find("*").attr('disabled', true)
          }, 500, false);

          let license = ArtifactoryFeatures.getAllowedLicense(feature);
          // Add the correct class:
          $($element).addClass('license-required-' + license);
          $($element).addClass('license-required');

          // Add a tooltip with link to the feature page:
          let featureName = ArtifactoryFeatures.getFeatureName(feature);
          let featureLink = ArtifactoryFeatures.getFeatureLink(feature);

          let edition = (currentLicense === 'ConanCE') ? `<br>Aritfactory Community Edition for C/C++` : 'OSS';

          $($element).tooltipster({
            animation: 'fade',
            contentAsHTML : 'true',
            trigger: 'hover',
            onlyOne: 'true',
            interactive: 'true',
            interactiveTolerance: 150,
            position: 'top',
            theme: 'tooltipster-default top',
            content: featureLink ? `Learn more about the <a href="${featureLink}" target="_blank">${featureName}</a> feature` : `${featureName} feature is not supported in ${edition} version`
          });
        } else if (feature === 'trustedkeys' && currentLicense != 'EDGE' && currentLicense != 'ENTPLUS') {
          return;
        }
        else {
          $($element).hide();
        }
      }
    }
  }
}