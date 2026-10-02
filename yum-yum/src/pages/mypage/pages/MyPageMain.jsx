import React, { useEffect, useState } from 'react';
import SettingIcon from '@/assets/icons/icon-setting.svg?react';
import { callUserUid } from '@/utils/localStorage';

import { useMyPageUserData } from '@/hooks/useMyPageUser';
import useDeleteUser from '@/hooks/useDeleteUser';

import MyPageGoalCard from '../component/MyPageGoalCard';
import MyPageCSItem from '../component/MyPageCSItem';
import TOSModal from '../component/TOSModal';
import ConfirmModal from '@/components/modal/ConfirmModal';

import useAuth from '@/hooks/useAuth';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';


export default function MyPageMain() {
  const userId = callUserUid();

  // 사용자 정보
  const { userName, goal, targetWeight, targetExercise, createDays } = useMyPageUserData(userId);

  // 로그아웃
  const { logout } = useAuth();

  // 탈퇴
  const { deleteUser } = useDeleteUser();

  // 모달
  const [openModal, setOpenModal] = useState(null);

  // 삭제확인 모달
  const [confirmModal, setConfirmModal] = useState(false);

  // 메일
  const handleSupportClick = () => {
    try {
      window.open('mailto:noreply@todays-yum-yum.firebaseapp.com');
    } catch (e) {
      toast.error('메일 앱을 열 수 없습니다. 기본 메일 프로그램이 설정되어 있는지 확인해주세요.');
    }
  };

  return (
    <div className='flex flex-col gap-5 px-5 justify-around item-center bg-gray-50 w-full h-[calc(100vh-122px)] overflow-y-auto'>
      <div className='flex flex-col gap-5'>
        {/* 상단 */}
        <div className='flex flex-col bg-white rounded-[20px] gap-7 p-7'>
          {/* 이름과 기록일 */}
          <div className='flex flex-row justify-between items-baseline'>
            <div>
              <span className='text-2xl text-primary font-bold'>{userName ? userName : ''} </span>
              <span className='text-base font-bold text-gray-400'>{'님'}</span>
            </div>

            <div className='text-center bg-secondary-light rounded-[8px] px-3.5 py-2'>
              <span className='text-base text-secondary font-extrabold'>
                {createDays ? createDays : 0}{' '}
              </span>
              <span className='text-base font-bold text-gray-500'>{'일 기록 중 📈'}</span>
            </div>
          </div>

          {/* 나의 목표 */}
          <div className='flex flex-col bg-primary-light rounded-[12px] gap-8 p-7'>
            <div className='flex flex-row justify-between items-center'>
              <span className='text-xl font-bold'>나의 목표</span>

              {/* 정보 수정 버튼 */}
              <Link to={'/mypage/update'} className='flex flex-col items-center gap-1'>
                <SettingIcon />
              </Link>
            </div>

            {/* 목표 카드 */}
            <div>
              <MyPageGoalCard
                goals={{
                  '목표 체중': `${targetWeight ?? 0} kg`,
                  '목표 설정': goal,
                  활동량: targetExercise,
                }}
              />
            </div>
          </div>
        </div>

        {/* 고객 지원 */}
        <div className='flex flex-col gap-5'>
          <div className='flex flex-col bg-white rounded-[20px] gap-7 p-7'>
            <div className='flex flex-col justify-between items-baseline gap-5'>
              <div>
                <span className='text-xl font-bold'>고객지원</span>
              </div>

              <MyPageCSItem label={'문의하기'} onItemClick={handleSupportClick} />

              <MyPageCSItem label={'서비스 이용약관'} type='service' setOpenModal={setOpenModal} />

              <MyPageCSItem
                label={'개인정보 처리 방침'}
                type='privacy'
                setOpenModal={setOpenModal}
              />

              <MyPageCSItem
                label={'민감정보 처리 방침'}
                type='sensitive'
                setOpenModal={setOpenModal}
              />

              <MyPageCSItem label={'회원 탈퇴'} setOpenModal={setConfirmModal} />
            </div>
          </div>

          <div className='flex justify-end'>
            <span className='text-sm text-gray-500 underline cursor-pointer' onClick={logout}>
              로그아웃
            </span>
          </div>
        </div>
      </div>

      <div className='flex justify-center'>
        <span className='text-center text-sm text-gray-500'>
          © 2025 TODAYS YUM YUM. All rights reserved.
        </span>
      </div>

       {/* 이용약관, 처리방침 */}
      {openModal && (
        <TOSModal
          isOpenModal={!!openModal}
          onCloseModal={() => setOpenModal(null)}
          type={openModal}
        />
      )}

      {/* 탈퇴 모달 */}
      <ConfirmModal
        isOpenModal={confirmModal}
        onCloseModal={() => setConfirmModal(false)}
        title='정말 탈퇴하시겠습니까?'
        desc='탈퇴 후 되돌릴 수 없습니다.'
        leftBtnLabel='취소'
        RightBtnLabel='탈퇴'
        onConfirm={deleteUser}
      />
    </div>
  );
}
