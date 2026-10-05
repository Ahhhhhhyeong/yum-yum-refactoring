// 사용자 정보 확인 및 수정 훅
import { useMemo, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useUserData } from './useUser';
import {
  gender,
  goalsOption,
  goalsOptionUtils,
  activityLevel,
  activityUtils,
} from '@/data/userContext';
import { editProfile } from '../services/userApi';
import { MIN_BIRTH_YEAR, getCurrentYear, getMaxYear } from '../utils/birthYear';
import toast from 'react-hot-toast';

// 커스텀 훅
export const useUserSettings = (userId) => {
  const queryClient = useQueryClient();
  // 사용자 설정 조회
  const { userData, isLoading } = useUserData(userId);

  // 설정 데이터 생성
  const { userSettings, defaultValues } = useMemo(() => {
    if (!userData) {
      return { userSettings: [], defaultValues: {} };
    }
    const settings = parsedUserSetting(userData);

    const defaults = settings.reduce((acc, item) => {
      // number 타입은 숫자만 추출, select는 그대로
      acc[item.id] = item.type === 'number' ? item.value.replace(/[^0-9.]/g, '') : item.key;
      return acc;
    }, {});

    return { userSettings: settings, defaultValues: defaults };
  }, [userData]);

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm({
    defaultValues,
    values: defaultValues,
  });

  // 모달 상태 관리
  const [modalState, setModalState] = useState({
    isOpen: false,
    currentItem: null,
  });

  // 설정 항목 클릭 핸들러
  const handleSettingClick = (itemId) => {
    const item = userSettings.find((item) => item.id === itemId);
    setModalState({
      isOpen: true,
      currentItem: item,
    });
  };

  // 모달 닫기
  const handleModalClose = () => {
    // 모달 닫을 때 원래 값으로 롤백
    reset(defaultValues);

    setModalState({
      isOpen: false,
      currentItem: null,
    });
  };

  // 저장 핸들러
  const handleSave = async (data) => {
    if (!isDirty) return;

    const { currentItem } = modalState;
    const newValue = data[currentItem.id];

    // newValue의 타입 구별
    let num = Number(newValue);
    num = Number.isNaN(num) ? newValue : num;

    // 업데이트 호출
    const edit = userSettingsMutation.mutateAsync({ currentItem, newValue: num });
    // toast 알람
    await toast.promise(edit, {
      loading: '수정하는 중...',
      success: (response) => {
        setModalState({ isOpen: false, currentItem: null });
        return response?.message || '성공적으로 수정되었습니다!';
      },
      error: (error) => {
        return error?.message || '수정에 실패했습니다.';
      },
    });
  };

  // 설정 업데이트 mutation
  const userSettingsMutation = useMutation({
    mutationFn: ({ currentItem, newValue }) => editProfile({ currentItem, newValue, userId }),
    onSuccess: (response) => {
      const { fieldName, newValue } = response.data;
      queryClient.setQueryData(['user', userId], (oldData) => {
        if (!oldData) return oldData;

        const newData = { ...oldData };

        // 중첩객체 처리
        if (fieldName.includes('.')) {
          // goals.~~~ 같은 경우
          const [parentKey, childKey] = fieldName.split('.');
          newData[parentKey] = {
            ...newData[parentKey],
            [childKey]: newValue,
          };
        } else {
          // 일반 필드
          newData[fieldName] = newValue;
        }

        newData.updatedAt = new Date();
        return newData;
      });

      queryClient.invalidateQueries({
        queryKey: ['user', userId],
      });
    },
    onError: (error) => {
      console.error('업데이트 에러: ', error);
    },
  });

  return {
    // 서버 상태
    userSettings,
    isLoading,

    // mutation 상태
    isSaving: userSettingsMutation.isPending,
    saveError: userSettingsMutation.error,

    // 클라이언트 상태
    modalState,

    // 핸들러들
    handleSettingClick,
    handleModalClose,

    // 모달 상태
    currentItem: modalState.currentItem,
    isModalOpen: modalState.isOpen,
    tempValue: modalState.tempValue,

    // hook form
    register,
    control,
    handleSubmit: handleSubmit(handleSave),
    reset,
    formErrors: errors,
    isDirty,
  };
};

const parsedUserSetting = (userData) => {
  if (!userData) return null;
  // 사용자 성별
  const usergender = gender[userData?.gender];
  // 사용자 목표 무게
  const goalWegiht = userData.goals['targetWeight'];
  // 사용자 목표
  const goal = goalsOptionUtils.getLabel(userData.goals['goal']);
  // 사용자 활동량
  const activity = activityUtils.getTitle(userData.goals['targetExercise']);
  
  const parsedUserSetting = (userData) => {
    if(!userData) return null;
    const maxYear = getMaxYear();
    return [
      {
        id: 'gender',
        label: '성별',
        key: userData.gender,
        value: usergender,
        type: 'select',
        options: gender,
      },
      {
        id: 'birthYear',
        label: '출생연도',
        value: `${userData.birthYear}`,
        type: 'number',
        unit: '년',
        min: MIN_BIRTH_YEAR,
        max: maxYear,
        validationRules: {
          min: { value: MIN_BIRTH_YEAR, message: '출생연도를 다시 확인해주세요.' }, 
          max: { value: maxYear, message: '15세 이상만 가입가능해요.' }, 
          pattern: {
            value: /^\d+$/,
            message: '숫자만 입력 가능합니다',
          },
        },
      },
      {
        id: 'height',
        label: '키',
        value: `${userData.height}`,
        type: 'number',
        unit: 'cm',
        min: 50,
        max: 250,
        validationRules: {
          min: { value: 50, message: '50cm 이상 입력해주세요.' },
          max: { value: 250, message: '250cm 이하로 입력해주세요.' },
          pattern: {
            value: /^(?:\d{1,3}(?:\.\d{1})?|\d{1,2})$/,
            message: '숫자만 입력 가능하며, 소수점 첫째 자리까지만 입력해주세요',
          },
        },
      },
      {
        id: 'targetWeight',
        label: '목표 체중',
        value: `${goalWegiht}`,
        type: 'number',
        unit: 'kg',
        min: 30,
        max: 200,
        validationRules: {
          min: { value: 20, message: '20kg 이상 입력해주세요.' },
          max: { value: 300, message: '300kg 이하로 입력해주세요.' },
          pattern: {
            value: /^(?:\d{1,3}(?:\.\d{1})?|\d{1,2})$/,
            message: '숫자만 입력 가능하며, 소수점 첫째 자리까지만 입력해주세요',
          },
        },
      },
      {
        id: 'goal',
        label: '목표 설정',
        key: userData.goals['goal'],
        value: goal,
        type: 'select',
        options: goalsOption,
      },
      {
        id: 'targetExercise',
        label: '활동량',
        key: userData.goals['targetExercise'],
        value: activity,
        type: 'select',
        options: activityLevel,
      },
    ];
  }
};
